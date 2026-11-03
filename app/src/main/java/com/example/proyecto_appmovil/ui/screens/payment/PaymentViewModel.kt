package com.example.proyecto_appmovil.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.ComprobantePago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val comprobante: ComprobantePago? = null,
    val folioInput: String = "",
    val montoInput: String = "2850.00",
    val fechaInput: String = "Hoy",
    val comprobanteAdjuntoNombre: String? = null,
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class PaymentViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        cargarPago()
    }

    fun cargarPago() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pago = repository.getComprobantePago()
            _uiState.update {
                it.copy(
                    comprobante = pago,
                    folioInput = pago.folio,
                    montoInput = "${pago.monto}",
                    fechaInput = pago.fechaPago,
                    isLoading = false
                )
            }
        }
    }

    fun onFolioChange(v: String) = _uiState.update { it.copy(folioInput = v, mensajeError = null) }
    fun onMontoChange(v: String) = _uiState.update { it.copy(montoInput = v) }
    fun onFechaChange(v: String) = _uiState.update { it.copy(fechaInput = v) }
    fun onComprobanteAdjunto(nombre: String) = _uiState.update { it.copy(comprobanteAdjuntoNombre = nombre) }

    fun registrarPago() {
        val s = _uiState.value
        if (s.folioInput.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Ingrese el folio o referencia del comprobante bancario") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            val monto = s.montoInput.toDoubleOrNull() ?: 2850.0
            val result = repository.registrarComprobantePago(
                folio = s.folioInput,
                monto = monto,
                fechaPago = s.fechaInput,
                comprobanteUri = s.comprobanteAdjuntoNombre ?: ""
            )

            result.onSuccess {
                val updated = repository.getComprobantePago()
                _uiState.update {
                    it.copy(
                        comprobante = updated,
                        isLoading = false,
                        mensajeExito = "¡Comprobante de pago registrado y enviado a verificación!"
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(isLoading = false, mensajeError = ex.message ?: "Error al registrar")
                }
            }
        }
    }
}
