package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.coroutineContext

data class AdminAspiranteDetailUiState(
    val aspiranteId: String? = null,
    val aspirante: Aspirante? = null,
    val documentos: List<Documento> = emptyList(),
    val observacionesInput: String = "",
    val documentoARechazarId: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDownloadingPdf: Boolean = false,
    val pdfPreviewFile: File? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
) {
    val ocupado: Boolean
        get() = isLoading || isSaving

    fun puedeRevisar(documentoId: String): Boolean =
        !ocupado && documentos.any { it.id == documentoId && it.estado == EstadoDocumento.SUBIDO }

    val documentosAprobados: Int
        get() = TipoDocumento.entries.filter(TipoDocumento::obligatorio).count { tipo ->
            documentos.any { it.tipo == tipo && it.estado == EstadoDocumento.VALIDADO }
        }

    val documentosAprobadosFaltantes: Int
        get() = TipoDocumento.entries.count(TipoDocumento::obligatorio) - documentosAprobados

    val puedeAprobar: Boolean
        get() = aspirante != null && aspirante.estadoInscripcion != EstadoInscripcion.ACEPTADO &&
            documentosAprobados == TipoDocumento.entries.count(TipoDocumento::obligatorio)
}

class AdminAspiranteDetailViewModel(
    private val repository: InscripcionesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAspiranteDetailUiState())
    val uiState: StateFlow<AdminAspiranteDetailUiState> = _uiState.asStateFlow()

    private val _guardados = MutableStateFlow(0L)
    val guardados: StateFlow<Long> = _guardados.asStateFlow()

    private var carga: Job? = null
    private var descarga: Job? = null
    private val guardadosEnCurso = mutableSetOf<String>()
    private val avisosPendientes = mutableMapOf<String, AvisoRevision>()

    init {
        seleccionarAspirante(savedStateHandle[ASPIRANTE_SELECCIONADO])
    }

    fun seleccionarAspirante(aspiranteId: String?) {
        val estado = _uiState.value
        if (aspiranteId == estado.aspiranteId) {
            if (aspiranteId != null && estado.aspirante == null && !estado.ocupado) {
                _uiState.update { it.copy(mensajeError = null) }
                cargarDetalle(aspiranteId)
            }
            return
        }

        carga?.cancel()
        descarga?.cancel()
        savedStateHandle[ASPIRANTE_SELECCIONADO] = aspiranteId
        val aviso = aspiranteId?.let(avisosPendientes::remove)
        _uiState.value = AdminAspiranteDetailUiState(
            aspiranteId = aspiranteId,
            isSaving = aspiranteId != null && aspiranteId in guardadosEnCurso,
            mensajeExito = aviso?.takeIf { it.exito }?.mensaje,
            mensajeError = aviso?.takeUnless { it.exito }?.mensaje
        )
        aspiranteId?.let(::cargarDetalle)
    }

    fun abrirRechazo(documentoId: String) {
        _uiState.update { estado ->
            if (estado.puedeRevisar(documentoId)) {
                estado.copy(documentoARechazarId = documentoId, observacionesInput = "")
            } else {
                estado
            }
        }
    }

    fun cerrarRechazo() {
        _uiState.update { it.copy(documentoARechazarId = null, observacionesInput = "") }
    }

    fun onObservacionesChange(valor: String) {
        _uiState.update { it.copy(observacionesInput = valor) }
    }

    fun validarDocumento(docId: String, aprobado: Boolean) {
        val estado = _uiState.value
        val aspiranteId = estado.aspiranteId ?: return
        if (!estado.puedeRevisar(docId)) return

        val observaciones = if (aprobado) "" else estado.observacionesInput.trim()
        if (!aprobado && observaciones.isBlank()) {
            _uiState.update { it.copy(mensajeExito = null, mensajeError = "Ingrese el motivo del rechazo") }
            return
        }

        guardarRevision(
            aspiranteId = aspiranteId,
            mensajeExito = if (aprobado) "Documento aprobado" else "Documento rechazado",
            mensajeError = "No se pudo guardar el dictamen"
        ) {
            repository.revisarDocumento(docId, aprobado, observaciones)
        }
    }

    fun aprobarAspirante() {
        val estado = _uiState.value
        val aspiranteId = estado.aspiranteId ?: return
        if (estado.ocupado || !estado.puedeAprobar) return

        guardarRevision(
            aspiranteId = aspiranteId,
            mensajeExito = "Trámite aprobado",
            mensajeError = "No se pudo aprobar el trámite"
        ) {
            repository.aprobarAspirante(aspiranteId)
        }
    }

    fun descargarPdf(documentoId: String, nombreArchivo: String?) {
        val estado = _uiState.value
        if (estado.ocupado || estado.isDownloadingPdf ||
            estado.documentos.none { it.id == documentoId && it.nombreArchivo != null }
        ) return

        _uiState.update { it.copy(isDownloadingPdf = true, mensajeError = null) }
        descarga = viewModelScope.launch {
            val resultado = repository.descargarDocumentoParaPrevisualizacion(documentoId, nombreArchivo)
            coroutineContext.ensureActive()
            resultado.onSuccess { file ->
                _uiState.update { it.copy(isDownloadingPdf = false, pdfPreviewFile = file) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(isDownloadingPdf = false, mensajeError = ex.message ?: "No se pudo descargar el PDF")
                }
            }
        }
    }

    fun cerrarPdfPreview() {
        _uiState.update { it.copy(pdfPreviewFile = null) }
    }

    private fun cargarDetalle(aspiranteId: String) {
        carga?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        carga = viewModelScope.launch {
            val resultado = resultadoSolicitud {
                val aspirante = checkNotNull(repository.getAspirantePorId(aspiranteId)) {
                    "No se encontró el aspirante"
                }
                aspirante to repository.getDocumentosAspirante(aspiranteId)
            }
            coroutineContext.ensureActive()
            resultado.onSuccess { (aspirante, documentos) ->
                _uiState.update { it.copy(aspirante = aspirante, documentos = documentos, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        aspirante = null,
                        documentos = emptyList(),
                        isLoading = false,
                        mensajeError = it.mensajeError ?: ex.message ?: "No se pudo cargar el expediente"
                    )
                }
            }
        }
    }

    private fun guardarRevision(
        aspiranteId: String,
        mensajeExito: String,
        mensajeError: String,
        guardar: suspend () -> Result<*>
    ) {
        if (!guardadosEnCurso.add(aspiranteId)) return
        _uiState.update {
            it.copy(
                isSaving = true,
                documentoARechazarId = null,
                observacionesInput = "",
                mensajeExito = null,
                mensajeError = null
            )
        }

        viewModelScope.launch {
            val resultado = try {
                guardar()
            } finally {
                guardadosEnCurso.remove(aspiranteId)
            }
            val aviso = if (resultado.isSuccess) {
                AvisoRevision(exito = true, mensaje = mensajeExito)
            } else {
                AvisoRevision(exito = false, mensaje = resultado.exceptionOrNull()?.message ?: mensajeError)
            }
            if (_uiState.value.aspiranteId == aspiranteId) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        mensajeExito = aviso.mensaje.takeIf { aviso.exito },
                        mensajeError = aviso.mensaje.takeUnless { aviso.exito }
                    )
                }
                if (aviso.exito) cargarDetalle(aspiranteId)
            } else {
                avisosPendientes[aspiranteId] = aviso
            }
            if (aviso.exito) _guardados.update { it + 1 }
        }
    }

    private data class AvisoRevision(val exito: Boolean, val mensaje: String)

    private companion object {
        const val ASPIRANTE_SELECCIONADO = "aspiranteSeleccionadoId"
    }
}
