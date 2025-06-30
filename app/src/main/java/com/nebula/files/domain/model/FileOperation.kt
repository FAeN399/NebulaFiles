package com.nebula.files.domain.model

import java.util.UUID

data class FileOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val sourcePaths: List<String>,
    val destinationPath: String? = null,
    val progress: Int = 0,
    val currentFile: String? = null,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val totalBytes: Long = 0,
    val processedBytes: Long = 0,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val error: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
) {
    val remainingTime: Long?
        get() {
            if (progress == 0 || isCompleted || isCancelled) return null
            val elapsed = System.currentTimeMillis() - startTime
            val estimatedTotal = elapsed * 100 / progress
            return estimatedTotal - elapsed
        }
}

enum class OperationType {
    COPY,
    MOVE,
    DELETE,
    COMPRESS,
    EXTRACT
}