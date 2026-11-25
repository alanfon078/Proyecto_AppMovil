package com.example.proyecto_appmovil.model

data class Aspirante(
    val id: String,
    val nombreCompleto: String,
    val email: String,
    val curp: String = "",
    val telefono: String = "",
    val carrera: String = "",
    val folioAspirante: String = "",
    val promedioBachillerato: Double? = null,
    val direccion: String = "",
    val estadoResidencia: String = "",
    val municipioResidencia: String = "",
    val estadoInscripcion: EstadoInscripcion = EstadoInscripcion.PENDIENTE,
    val rol: String = "aspirante"
)

enum class EstadoInscripcion(val descripcion: String) {
    PENDIENTE("En Captura"),
    EN_REVISION("En Revisión por Control Escolar"),
    CON_OBSERVACIONES("Con Observaciones"),
    ACEPTADO("Documentación Aprobada"),
    RECHAZADO("Inscripción Rechazada")
}
