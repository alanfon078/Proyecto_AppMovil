package com.example.proyecto_appmovil.model

data class Aspirante(
    val id: String = "1",
    val nombreCompleto: String = "Dafne Guadalupe Orozco Aguirre",
    val curp: String = "OOAD030515HGTRRN01",
    val email: String = "aspirante@itsur.edu.mx",
    val telefono: String = "4451234567",
    val carrera: String = "Ing. Sistemas Computacionales",
    val folioAspirante: String = "ITSUR-2026-0042",
    val promedioBachillerato: Double = 9.2,
    val direccion: String = "Calle Hidalgo #123, Uriangato, Gto.",
    val estadoInscripcion: EstadoInscripcion = EstadoInscripcion.EN_REVISION,
    val rol: String = "aspirante" // "aspirante" o "escolar"
)

enum class EstadoInscripcion(val descripcion: String) {
    PENDIENTE("En Captura"),
    EN_REVISION("En Revisión por Control Escolar"),
    CON_OBSERVACIONES("Con Observaciones"),
    ACEPTADO("Documentación Aprobada"),
    RECHAZADO("Inscripción Rechazada")
}
