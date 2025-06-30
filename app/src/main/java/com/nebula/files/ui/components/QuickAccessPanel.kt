package com.nebula.files.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nebula.files.data.provider.TermuxFileProvider

@Composable
fun QuickAccessPanel(
    onPathSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TermuxFileProvider.QUICK_ACCESS_PATHS.forEach { (name, path) ->
            QuickAccessItem(
                name = name,
                path = path,
                icon = getQuickAccessIcon(name),
                onClick = { onPathSelected(path) }
            )
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text(
            text = "Storage Sources",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        QuickAccessItem(
            name = "Google Drive",
            path = "",
            icon = Icons.Default.CloudQueue,
            onClick = { /* TODO: Switch to Google Drive */ }
        )
    }
}

@Composable
private fun QuickAccessItem(
    name: String,
    path: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (path.isNotEmpty()) {
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getQuickAccessIcon(name: String): ImageVector {
    return when (name) {
        "Home" -> Icons.Default.Home
        "Downloads" -> Icons.Default.Download
        "Storage" -> Icons.Default.Storage
        "Prefix" -> Icons.Default.Folder
        "Bin" -> Icons.Default.Terminal
        "Share" -> Icons.Default.Share
        "SD Card" -> Icons.Default.SdCard
        "External" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Folder
    }
}