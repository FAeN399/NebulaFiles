package com.nebula.files.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.lifecycleScope
import com.nebula.files.core.QuantumLifecycleService
import com.nebula.files.MainActivity
import com.nebula.files.R
import com.nebula.files.data.repository.FileOperationRepository
import com.nebula.files.domain.model.FileOperation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HiltFileOperationService : QuantumLifecycleService() {
    
    @Inject
    lateinit var fileOperationRepository: FileOperationRepository
    
    companion object {
        const val CHANNEL_ID = "file_operations"
        const val NOTIFICATION_ID = 1
        
        const val ACTION_COPY = "com.nebula.files.ACTION_COPY"
        const val ACTION_MOVE = "com.nebula.files.ACTION_MOVE"
        const val ACTION_DELETE = "com.nebula.files.ACTION_DELETE"
        const val ACTION_CANCEL = "com.nebula.files.ACTION_CANCEL"
        
        const val EXTRA_SOURCE_PATHS = "source_paths"
        const val EXTRA_DESTINATION_PATH = "destination_path"
        const val EXTRA_OPERATION_ID = "operation_id"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_COPY -> handleCopyOperation(intent)
            ACTION_MOVE -> handleMoveOperation(intent)
            ACTION_DELETE -> handleDeleteOperation(intent)
            ACTION_CANCEL -> handleCancelOperation(intent)
        }
        
        return START_NOT_STICKY
    }
    
    private fun handleCopyOperation(intent: Intent) {
        val sourcePaths = intent.getStringArrayListExtra(EXTRA_SOURCE_PATHS) ?: return
        val destinationPath = intent.getStringExtra(EXTRA_DESTINATION_PATH) ?: return
        
        startForegroundService()
        
        lifecycleScope.launch {
            fileOperationRepository.copyFiles(sourcePaths, destinationPath)
                .collectLatest { operation ->
                    updateNotification(operation)
                    
                    if (operation.isCompleted || operation.isCancelled || operation.error != null) {
                        stopSelf()
                    }
                }
        }
    }
    
    private fun handleMoveOperation(intent: Intent) {
        val sourcePaths = intent.getStringArrayListExtra(EXTRA_SOURCE_PATHS) ?: return
        val destinationPath = intent.getStringExtra(EXTRA_DESTINATION_PATH) ?: return
        
        startForegroundService()
        
        lifecycleScope.launch {
            fileOperationRepository.moveFiles(sourcePaths, destinationPath)
                .collectLatest { operation ->
                    updateNotification(operation)
                    
                    if (operation.isCompleted || operation.isCancelled || operation.error != null) {
                        stopSelf()
                    }
                }
        }
    }
    
    private fun handleDeleteOperation(intent: Intent) {
        val sourcePaths = intent.getStringArrayListExtra(EXTRA_SOURCE_PATHS) ?: return
        
        startForegroundService()
        
        lifecycleScope.launch {
            fileOperationRepository.deleteFiles(sourcePaths)
                .collectLatest { operation ->
                    updateNotification(operation)
                    
                    if (operation.isCompleted || operation.isCancelled || operation.error != null) {
                        stopSelf()
                    }
                }
        }
    }
    
    private fun handleCancelOperation(intent: Intent) {
        val operationId = intent.getStringExtra(EXTRA_OPERATION_ID) ?: return
        
        lifecycleScope.launch {
            fileOperationRepository.cancelOperation(operationId)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    private fun startForegroundService() {
        val notification = createNotification(
            title = "File operation starting...",
            progress = 0
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
    
    private fun updateNotification(operation: FileOperation) {
        val title = when {
            operation.error != null -> "Operation failed"
            operation.isCancelled -> "Operation cancelled"
            operation.isCompleted -> "Operation completed"
            else -> "${operation.type.name} in progress"
        }
        
        val notification = createNotification(
            title = title,
            content = operation.currentFile ?: "",
            progress = operation.progress,
            showCancel = !operation.isCompleted && !operation.isCancelled,
            operationId = operation.id
        )
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(
        title: String,
        content: String = "",
        progress: Int = 0,
        showCancel: Boolean = false,
        operationId: String? = null
    ): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
        
        if (progress > 0) {
            builder.setProgress(100, progress, false)
        }
        
        if (showCancel && operationId != null) {
            val cancelIntent = Intent(this, HiltFileOperationService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_OPERATION_ID, operationId)
            }
            val cancelPendingIntent = PendingIntent.getService(
                this,
                1,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                cancelPendingIntent
            )
        }
        
        return builder.build()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "File Operations",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress for file operations"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}