package com.example.proyecto_appmovil.ui.screens.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CredentialPhotoUiState(
    val photoUriString: String? = null,
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CredentialPhotoViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CredentialPhotoUiState())
    val uiState: StateFlow<CredentialPhotoUiState> = _uiState.asStateFlow()

    fun onPhotoSelected(uriString: String) {
        _uiState.update { it.copy(photoUriString = uriString, isSuccess = false, errorMessage = null) }
    }

    fun guardarFoto() {
        val uri = _uiState.value.photoUriString ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            // Simulación de guardado
            _uiState.update {
                it.copy(
                    isUploading = false,
                    isSuccess = true
                )
            }
        }
    }
}
