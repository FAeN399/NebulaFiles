package com.nebula.files.domain.model

import java.util.Date

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Date,
    val mimeType: String?,
    val source: FileSource,
    val permissions: String? = null,
    val isHidden: Boolean = false,
    val isSymlink: Boolean = false,
    val symlinkTarget: String? = null,
    val driveId: String? = null,
    val canWrite: Boolean = true,
    val canRead: Boolean = true,
    val canExecute: Boolean = false
) {
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()
    
    val formattedSize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
}

sealed class FileSource {
    data object LocalStorage : FileSource()
    data class TermuxHome(val basePath: String = "/data/data/com.termux/files/home") : FileSource()
    data class TermuxPrefix(val basePath: String = "/data/data/com.termux/files/usr") : FileSource()
    data class GoogleDrive(val accountEmail: String) : FileSource()
    data class SAF(val uri: String) : FileSource()
}