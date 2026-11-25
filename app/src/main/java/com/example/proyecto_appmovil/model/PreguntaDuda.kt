package com.example.proyecto_appmovil.model

enum class CategoriaPregunta(val valorApi: String, val etiqueta: String) {
    FECHAS("fechas", "Fechas"),
    DOCUMENTOS("documentos", "Documentos");

    companion object {
        fun fromApi(valor: String?): CategoriaPregunta? =
            entries.firstOrNull { it.valorApi.equals(valor, ignoreCase = true) }
    }
}

enum class EstadoPregunta(val valorApi: String, val etiqueta: String) {
    PENDIENTE("pendiente", "Pendiente"),
    RESPONDIDA("respondida", "Respondida");

    companion object {
        fun fromApi(valor: String?): EstadoPregunta =
            entries.firstOrNull { it.valorApi.equals(valor, ignoreCase = true) } ?: PENDIENTE
    }
}

data class PreguntaDuda(
    val id: String = "",
    val nombreCompleto: String = "",
    val folio: String = "",
    val carrera: String = "",
    val categoria: CategoriaPregunta = CategoriaPregunta.FECHAS,
    val pregunta: String = "",
    val estado: EstadoPregunta = EstadoPregunta.PENDIENTE,
    val respuesta: String? = null,
    val fechaCreacion: String = "",
    val fechaRespuesta: String? = null
)
