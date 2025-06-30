package com.nebula.files.data.provider

import com.nebula.files.domain.model.FileItem
import com.nebula.files.domain.model.FileSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TermuxFileProvider @Inject constructor() {
    
    companion object {
        val TERMUX_HOME = System.getenv("HOME") ?: "/data/data/com.termux/files/home"
        val TERMUX_PREFIX = System.getenv("PREFIX") ?: "/data/data/com.termux/files/usr"
        val TERMUX_DOWNLOADS = "$TERMUX_HOME/downloads"
        val TERMUX_STORAGE = "$TERMUX_HOME/storage"
        
        val QUICK_ACCESS_PATHS = listOf(
            "Home" to TERMUX_HOME,
            "Downloads" to TERMUX_DOWNLOADS,
            "Storage" to TERMUX_STORAGE,
            "Prefix" to TERMUX_PREFIX,
            "Bin" to "$TERMUX_PREFIX/bin",
            "Share" to "$TERMUX_PREFIX/share",
            "SD Card" to "/sdcard",
            "External" to "/storage/emulated/0"
        )
    }
    
    suspend fun listFiles(path: String): Flow<List<FileItem>> = flow {
        emit(getFilesInDirectory(path))
    }
    
    private suspend fun getFilesInDirectory(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }
        
        val files = directory.listFiles() ?: return@withContext emptyList()
        
        files.map { file ->
            val fileSource = when {
                file.absolutePath.startsWith(TERMUX_HOME) -> FileSource.TermuxHome()
                file.absolutePath.startsWith(TERMUX_PREFIX) -> FileSource.TermuxPrefix()
                else -> FileSource.LocalStorage
            }
            
            val permissions = try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    val path = Paths.get(file.absolutePath)
                    val attrs = Files.readAttributes(path, "posix:*")
                    val perms = attrs["permissions"] as? Set<*>
                    perms?.let { PosixFilePermissions.toString(it as Set<java.nio.file.attribute.PosixFilePermission>) }
                } else {
                    buildString {
                        append(if (file.canRead()) "r" else "-")
                        append(if (file.canWrite()) "w" else "-")
                        append(if (file.canExecute()) "x" else "-")
                    }
                }
            } catch (e: Exception) {
                null
            }
            
            val isSymlink = try {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    Files.isSymbolicLink(Paths.get(file.absolutePath))
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
            
            val symlinkTarget = if (isSymlink && android.os.Build.VERSION.SDK_INT >= 26) {
                try {
                    Files.readSymbolicLink(Paths.get(file.absolutePath)).toString()
                } catch (e: Exception) {
                    null
                }
            } else null
            
            FileItem(
                id = file.absolutePath,
                name = file.name,
                path = file.absolutePath,
                size = if (file.isFile) file.length() else 0,
                isDirectory = file.isDirectory,
                lastModified = Date(file.lastModified()),
                mimeType = getMimeType(file),
                source = fileSource,
                permissions = permissions,
                isHidden = file.name.startsWith("."),
                isSymlink = isSymlink,
                symlinkTarget = symlinkTarget,
                canRead = file.canRead(),
                canWrite = file.canWrite(),
                canExecute = file.canExecute()
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
    
    private fun getMimeType(file: File): String? {
        if (file.isDirectory) return null
        
        return when (file.extension.lowercase()) {
            "txt", "log", "md" -> "text/plain"
            "sh", "bash", "zsh" -> "application/x-sh"
            "py" -> "text/x-python"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "text/xml"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "kt" -> "text/x-kotlin"
            "java" -> "text/x-java"
            "c" -> "text/x-c"
            "cpp", "cc", "cxx" -> "text/x-c++"
            "h", "hpp" -> "text/x-c-header"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }
    
    suspend fun createDirectory(parentPath: String, name: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val newDir = File(parentPath, name)
            if (newDir.exists()) {
                return@withContext Result.failure(Exception("Directory already exists"))
            }
            
            if (!newDir.mkdirs()) {
                return@withContext Result.failure(Exception("Failed to create directory"))
            }
            
            Result.success(getFilesInDirectory(parentPath).first { it.name == name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun renameFile(oldPath: String, newName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val oldFile = File(oldPath)
            val newFile = File(oldFile.parent, newName)
            
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("File already exists"))
            }
            
            if (!oldFile.renameTo(newFile)) {
                return@withContext Result.failure(Exception("Failed to rename file"))
            }
            
            Result.success(getFilesInDirectory(newFile.parent).first { it.name == newName })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}