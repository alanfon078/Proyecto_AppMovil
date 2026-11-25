package com.example.proyecto_appmovil.model

data class FechaImportante(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: String,
    val diasRestantes: Int,
    val tipo: String = "limite_entrega"
)

enum class TipoFechaImportante(val valorApi: String, val etiqueta: String) {
    LIMITE_ENTREGA("limite_entrega", "Límite de entrega"),
    CORRECCIONES("correcciones", "Correcciones"),
    REVISION("revision", "Revisión"),
    PUBLICACION("publicacion", "Publicación"),
    OTRO("otro", "Otro");

    companion object {
        fun fromApi(valor: String): TipoFechaImportante =
            entries.firstOrNull { it.valorApi.equals(valor.trim(), ignoreCase = true) } ?: OTRO
    }
}
