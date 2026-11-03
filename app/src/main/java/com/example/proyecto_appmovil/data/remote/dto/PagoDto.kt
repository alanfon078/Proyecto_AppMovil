package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PagoRequestDto(
    @SerializedName("folioPago")
    val folioPago: String,
    @SerializedName("monto")
    val monto: Double,
    @SerializedName("fechaPago")
    val fechaPago: String
)

data class PagoResponseDto(
    @SerializedName("idPago")
    val idPago: Long,
    @SerializedName("idAspirante")
    val idAspirante: Long,
    @SerializedName("folioPago")
    val folioPago: String,
    @SerializedName("monto")
    val monto: Double,
    @SerializedName("fechaPago")
    val fechaPago: String,
    @SerializedName("urlComprobante")
    val urlComprobante: String? = null,
    @SerializedName("estado")
    val estado: String = "PENDIENTE",
    @SerializedName("observaciones")
    val observaciones: String? = null
)
