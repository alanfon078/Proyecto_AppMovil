package com.example.proyecto_appmovil.model

data class Aspirante(
    val id: String = "1",
    val nombreCompleto: String,
    val curp: String,
    val email: String,
    val telefono: String,
    val carrera: String,
    val folioAspirante: String,
    val promedioBachillerato: Double = 0.0,
    val direccion: String = "",
    val estadoInscripcion: EstadoInscripcion = EstadoInscripcion.EN_REVISION
)

enum class EstadoInscripcion(val descripcion: String) {
    PENDIENTE("Pendiente de Documentos"),
    EN_REVISION("En Revisión por Control Escolar"),
    CON_OBSERVACIONES("Con Observaciones"),
    ACEPTADO("Inscripción Aceptada"),
    RECHAZADO("Inscripción Rechazada")
}
