package com.nebula.files.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nebula.files.domain.model.FileItem
import com.nebula.files.domain.model.FileSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    file: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileIcon(file),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = getIconTint(file)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    when (file.source) {
                        is FileSource.TermuxHome -> {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge { Text("Termux") }
                        }
                        is FileSource.GoogleDrive -> {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge { Text("Drive") }
                        }
                        else -> {}
                    }
                }
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!file.isDirectory) {
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    file.permissions?.let { perms ->
                        Text(
                            text = perms,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (file.isSymlink) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "Symlink",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getFileIcon(file: FileItem): ImageVector {
    return when {
        file.isDirectory -> Icons.Default.Folder
        file.extension == "sh" || file.extension == "bash" -> Icons.Default.Terminal
        file.extension in listOf("txt", "md", "log") -> Icons.Default.Description
        file.extension in listOf("jpg", "jpeg", "png", "gif") -> Icons.Default.Image
        file.extension in listOf("mp4", "avi", "mkv") -> Icons.Default.Movie
        file.extension in listOf("mp3", "wav", "flac") -> Icons.Default.MusicNote
        file.extension == "apk" -> Icons.Default.Android
        file.extension in listOf("zip", "tar", "gz", "7z") -> Icons.Default.FolderZip
        file.extension in listOf("pdf") -> Icons.Default.PictureAsPdf
        file.canExecute -> Icons.Default.PlayArrow
        else -> Icons.Default.InsertDriveFile
    }
}

@Composable
private fun getIconTint(file: FileItem): androidx.compose.ui.graphics.Color {
    return when {
        file.isDirectory -> MaterialTheme.colorScheme.primary
        file.canExecute -> MaterialTheme.colorScheme.tertiary
        file.extension in listOf("sh", "bash", "py") -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}