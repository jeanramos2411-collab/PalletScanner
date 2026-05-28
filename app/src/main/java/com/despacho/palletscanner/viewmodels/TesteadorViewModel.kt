package com.despacho.palletscanner.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import com.despacho.palletscanner.data.services.SignalRService
import com.despacho.palletscanner.data.models.PalletInfoResponse
import com.despacho.palletscanner.data.models.DeletionResult

class TesteadorViewModel(
    private val signalRService: SignalRService
) : ViewModel() {

    companion object {
        private const val TAG = "TesteadorViewModel"
    }

    // Exponer estados de conexion
    val connectionState = signalRService.connectionState

    // Exponer respuestas del Testeador desde SignalRService
    val palletInfoResponse: StateFlow<PalletInfoResponse?> = signalRService.palletInfoResponse
    val palletDeletionResult: StateFlow<DeletionResult?> = signalRService.palletDeletionResult

    // Estado de busqueda
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Estado de eliminacion
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Numero de pallet ingresado
    private val _palletNumber = MutableStateFlow("")
    val palletNumber: StateFlow<String> = _palletNumber.asStateFlow()

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Dialog de confirmacion de eliminacion
    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    // Mensaje de exito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Observar resultados de busqueda exitosa
        viewModelScope.launch {
            palletInfoResponse.collect { response ->
                if (response != null && _isSearching.value) {
                    _isSearching.value = false
                    _errorMessage.value = null
                    Log.d(TAG, "📦 Respuesta de info recibida exitosamente")
                }
            }
        }

        // Observar errores del Testeador desde el servidor
        viewModelScope.launch {
            signalRService.testeadorError.collect { error ->
                if (error != null && _isSearching.value) {
                    _isSearching.value = false
                    _errorMessage.value = error
                    Log.d(TAG, "⚠️ Error del servidor: $error")
                }
            }
        }

        // Observar resultados de eliminacion
        viewModelScope.launch {
            palletDeletionResult.collect { result ->
                if (result != null && _isDeleting.value) {
                    _isDeleting.value = false
                    if (result.success) {
                        _successMessage.value = result.message
                        clearResults()
                    } else {
                        _errorMessage.value = result.message
                    }
                    Log.d(TAG, "🗑️ Resultado eliminacion: ${result.success} - ${result.message}")
                }
            }
        }
    }

    fun updatePalletNumber(number: String) {
        _palletNumber.value = number
        _errorMessage.value = null
    }

    fun searchPallet() {
        val number = _palletNumber.value.trim()
        if (number.isEmpty()) {
            _errorMessage.value = "Ingrese un numero de pallet"
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null
            _successMessage.value = null
            signalRService.clearTesteadorStates()

            Log.d(TAG, "🔍 Buscando pallet: $number")
            signalRService.requestPalletInfo(number)
        }
    }

    fun requestDeletePallet() {
        if (palletInfoResponse.value == null) return
        _showDeleteConfirmDialog.value = true
    }

    fun confirmDeletePallet() {
        _showDeleteConfirmDialog.value = false
        val number = _palletNumber.value.trim()
        if (number.isEmpty()) return

        viewModelScope.launch {
            _isDeleting.value = true
            _errorMessage.value = null
            _successMessage.value = null

            Log.d(TAG, "🗑️ Solicitando eliminacion de pallet: $number")
            signalRService.requestPalletDeletion(number)
        }
    }

    fun dismissDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = false
    }

    fun clearResults() {
        signalRService.clearTesteadorStates()
        _palletNumber.value = ""
        _errorMessage.value = null
        _successMessage.value = null
        _isSearching.value = false
        _isDeleting.value = false
        Log.d(TAG, "🧹 Resultados limpiados")
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun dismissSuccess() {
        _successMessage.value = null
    }
}
