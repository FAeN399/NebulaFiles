package com.nebula.files.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nebula.files.data.provider.TermuxFileProvider
import com.nebula.files.domain.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val fileProvider: TermuxFileProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()
    
    init {
        navigateToPath(TermuxFileProvider.TERMUX_HOME)
    }
    
    fun navigateToPath(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                fileProvider.listFiles(path)
                    .collect { files ->
                        _uiState.update { 
                            it.copy(
                                files = files,
                                currentPath = path,
                                isLoading = false,
                                error = null,
                                selectedFileId = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    fun onFileClick(file: FileItem) {
        if (file.isDirectory) {
            navigateToPath(file.path)
        } else {
            _uiState.update { it.copy(selectedFileId = file.id) }
        }
    }
    
    fun onFileLongClick(file: FileItem) {
        _uiState.update { it.copy(selectedFileId = file.id) }
    }
    
    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
    
    fun refresh() {
        navigateToPath(_uiState.value.currentPath)
    }
}

data class FileListUiState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFileId: String? = null,
    val isGridView: Boolean = false
)