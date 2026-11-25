package com.example.proyecto_appmovil.ui

import kotlinx.coroutines.CancellationException

internal inline fun <T> resultadoSolicitud(operacion: () -> T): Result<T> = try {
    Result.success(operacion())
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (error: Exception) {
    Result.failure(error)
}
