package com.example.proyecto_appmovil.model

data class Documento(
    val id: String,
    val tipo: TipoDocumento,
    val nombreArchivo: String? = null,
    val uriString: String? = null,
    val tamanoBytes: Long = 0,
    val fechaSubida: String? = null,
    val estado: EstadoDocumento = EstadoDocumento.PENDIENTE,
    val observaciones: String = "",
    val version: Int = 1
)

enum class TipoDocumento(val titulo: String, val descripcion: String, val obligatorio: Boolean) {
    ACTA_NACIMIENTO("Acta de Nacimiento", "Formato PDF legible y reciente", true),
    CERTIFICADO_BACHILLERATO("Certificado de Bachillerato", "O constancia de estudios con promedio", true),
    CURP("CURP Certificada", "Formato actualizado emitido por RENAPO", true),
    COMPROBANTE_DOMICILIO("Comprobante de Domicilio", "Agua, luz o teléfono (máximo 3 meses)", true),
    FOTO_INFANTIL("Fotografía Digital", "Fondo blanco, tipo credencial", true),
    COMPROBANTE_PAGO("Comprobante de Pago", "Ficha bancaria con sello o transferencia", true),
    REGLAMENTO_FIRMADO("Reglamento Institucional", "Firmado por el alumno o tutor", false)
}

enum class EstadoDocumento(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    SUBIDO("Subido (En revisión)"),
    VALIDADO("Aceptado"),
    RECHAZADO("Rechazado")
}
