package com.nebula.files.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nebula.files.R
import com.nebula.files.ui.components.FileItemRow
import com.nebula.files.ui.components.QuickAccessPanel
import com.nebula.files.ui.viewmodels.FileListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    navController: NavController,
    isExpandedScreen: Boolean,
    viewModel: FileListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                QuickAccessPanel(
                    onPathSelected = { path ->
                        viewModel.navigateToPath(path)
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("NebulaFiles")
                            if (uiState.currentPath.isNotEmpty()) {
                                Text(
                                    text = uiState.currentPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::toggleViewMode) {
                            Icon(
                                if (uiState.isGridView) Icons.Default.List else Icons.Default.Apps,
                                contentDescription = "Toggle view"
                            )
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: Add new file/folder */ },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New") }
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = viewModel::refresh) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.files.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Empty folder",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    if (isExpandedScreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(uiState.files, key = { it.id }) { file ->
                                    FileItemRow(
                                        file = file,
                                        isSelected = file.id == uiState.selectedFileId,
                                        onClick = { viewModel.onFileClick(file) },
                                        onLongClick = { viewModel.onFileLongClick(file) }
                                    )
                                }
                            }
                            
                            uiState.selectedFileId?.let { selectedId ->
                                val selectedFile = uiState.files.find { it.id == selectedId }
                                selectedFile?.let { file ->
                                    Surface(
                                        modifier = Modifier
                                            .width(400.dp)
                                            .fillMaxHeight(),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Size: ${file.formattedSize}")
                                            Text("Modified: ${file.lastModified}")
                                            file.permissions?.let {
                                                Text("Permissions: $it")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(uiState.files, key = { it.id }) { file ->
                                FileItemRow(
                                    file = file,
                                    isSelected = file.id == uiState.selectedFileId,
                                    onClick = { viewModel.onFileClick(file) },
                                    onLongClick = { viewModel.onFileLongClick(file) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}