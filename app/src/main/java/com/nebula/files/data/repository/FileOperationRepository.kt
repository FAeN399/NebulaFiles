package com.nebula.files.data.repository

import com.nebula.files.domain.model.FileOperation
import com.nebula.files.domain.model.OperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileOperationRepository @Inject constructor() {
    
    private val activeOperations = ConcurrentHashMap<String, MutableStateFlow<FileOperation>>()
    
    suspend fun copyFiles(
        sourcePaths: List<String>,
        destinationPath: String
    ): Flow<FileOperation> = flow {
        val operation = FileOperation(
            type = OperationType.COPY,
            sourcePaths = sourcePaths,
            destinationPath = destinationPath,
            totalFiles = countFiles(sourcePaths),
            totalBytes = calculateTotalSize(sourcePaths)
        )
        
        val operationFlow = MutableStateFlow(operation)
        activeOperations[operation.id] = operationFlow
        
        emit(operation)
        
        try {
            performCopy(operation, operationFlow)
        } catch (e: Exception) {
            val errorOperation = operationFlow.value.copy(
                error = e.message,
                endTime = System.currentTimeMillis()
            )
            operationFlow.value = errorOperation
            emit(errorOperation)
        } finally {
            activeOperations.remove(operation.id)
        }
    }
    
    suspend fun moveFiles(
        sourcePaths: List<String>,
        destinationPath: String
    ): Flow<FileOperation> = flow {
        val operation = FileOperation(
            type = OperationType.MOVE,
            sourcePaths = sourcePaths,
            destinationPath = destinationPath,
            totalFiles = countFiles(sourcePaths),
            totalBytes = calculateTotalSize(sourcePaths)
        )
        
        val operationFlow = MutableStateFlow(operation)
        activeOperations[operation.id] = operationFlow
        
        emit(operation)
        
        try {
            performMove(operation, operationFlow)
        } catch (e: Exception) {
            val errorOperation = operationFlow.value.copy(
                error = e.message,
                endTime = System.currentTimeMillis()
            )
            operationFlow.value = errorOperation
            emit(errorOperation)
        } finally {
            activeOperations.remove(operation.id)
        }
    }
    
    suspend fun deleteFiles(sourcePaths: List<String>): Flow<FileOperation> = flow {
        val operation = FileOperation(
            type = OperationType.DELETE,
            sourcePaths = sourcePaths,
            totalFiles = countFiles(sourcePaths)
        )
        
        val operationFlow = MutableStateFlow(operation)
        activeOperations[operation.id] = operationFlow
        
        emit(operation)
        
        try {
            performDelete(operation, operationFlow)
        } catch (e: Exception) {
            val errorOperation = operationFlow.value.copy(
                error = e.message,
                endTime = System.currentTimeMillis()
            )
            operationFlow.value = errorOperation
            emit(errorOperation)
        } finally {
            activeOperations.remove(operation.id)
        }
    }
    
    suspend fun cancelOperation(operationId: String) {
        activeOperations[operationId]?.let { flow ->
            flow.value = flow.value.copy(
                isCancelled = true,
                endTime = System.currentTimeMillis()
            )
        }
    }
    
    private suspend fun performCopy(
        operation: FileOperation,
        operationFlow: MutableStateFlow<FileOperation>
    ) = withContext(Dispatchers.IO) {
        var processedFiles = 0
        var processedBytes = 0L
        
        operation.sourcePaths.forEach { sourcePath ->
            if (operationFlow.value.isCancelled) return@withContext
            
            val sourceFile = File(sourcePath)
            val destFile = File(operation.destinationPath, sourceFile.name)
            
            copyFileRecursive(sourceFile, destFile) { file, bytes ->
                processedFiles++
                processedBytes += bytes
                
                val progress = ((processedBytes * 100) / operation.totalBytes).toInt()
                
                operationFlow.value = operationFlow.value.copy(
                    currentFile = file.name,
                    processedFiles = processedFiles,
                    processedBytes = processedBytes,
                    progress = progress.coerceIn(0, 100)
                )
            }
        }
        
        operationFlow.value = operationFlow.value.copy(
            isCompleted = true,
            progress = 100,
            endTime = System.currentTimeMillis()
        )
    }
    
    private suspend fun performMove(
        operation: FileOperation,
        operationFlow: MutableStateFlow<FileOperation>
    ) = withContext(Dispatchers.IO) {
        // First try to rename (fast move)
        var allMovedByRename = true
        
        operation.sourcePaths.forEach { sourcePath ->
            if (operationFlow.value.isCancelled) return@withContext
            
            val sourceFile = File(sourcePath)
            val destFile = File(operation.destinationPath, sourceFile.name)
            
            if (!sourceFile.renameTo(destFile)) {
                allMovedByRename = false
            }
        }
        
        if (allMovedByRename) {
            operationFlow.value = operationFlow.value.copy(
                isCompleted = true,
                progress = 100,
                endTime = System.currentTimeMillis()
            )
        } else {
            // Fall back to copy + delete
            performCopy(operation, operationFlow)
            
            if (operationFlow.value.isCompleted && !operationFlow.value.isCancelled) {
                operation.sourcePaths.forEach { path ->
                    File(path).deleteRecursively()
                }
            }
        }
    }
    
    private suspend fun performDelete(
        operation: FileOperation,
        operationFlow: MutableStateFlow<FileOperation>
    ) = withContext(Dispatchers.IO) {
        var processedFiles = 0
        
        operation.sourcePaths.forEach { path ->
            if (operationFlow.value.isCancelled) return@withContext
            
            val file = File(path)
            
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            
            processedFiles++
            val progress = (processedFiles * 100) / operation.totalFiles
            
            operationFlow.value = operationFlow.value.copy(
                currentFile = file.name,
                processedFiles = processedFiles,
                progress = progress
            )
        }
        
        operationFlow.value = operationFlow.value.copy(
            isCompleted = true,
            progress = 100,
            endTime = System.currentTimeMillis()
        )
    }
    
    private suspend fun copyFileRecursive(
        source: File,
        destination: File,
        onProgress: (File, Long) -> Unit
    ) {
        if (source.isDirectory) {
            destination.mkdirs()
            source.listFiles()?.forEach { child ->
                copyFileRecursive(
                    child,
                    File(destination, child.name),
                    onProgress
                )
            }
        } else {
            copyFile(source, destination)
            onProgress(source, source.length())
        }
    }
    
    private suspend fun copyFile(source: File, destination: File) = withContext(Dispatchers.IO) {
        destination.parentFile?.mkdirs()
        
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
        
        // Preserve file attributes
        destination.setLastModified(source.lastModified())
        destination.setExecutable(source.canExecute())
        destination.setWritable(source.canWrite())
        destination.setReadable(source.canRead())
    }
    
    private fun countFiles(paths: List<String>): Int {
        return paths.sumOf { path ->
            val file = File(path)
            when {
                file.isDirectory -> file.walkTopDown().count { it.isFile }
                else -> 1
            }
        }
    }
    
    private fun calculateTotalSize(paths: List<String>): Long {
        return paths.sumOf { path ->
            val file = File(path)
            when {
                file.isDirectory -> file.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                else -> file.length()
            }
        }
    }
}