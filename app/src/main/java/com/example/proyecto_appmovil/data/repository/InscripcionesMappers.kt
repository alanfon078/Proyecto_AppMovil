package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity
import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity
import com.example.proyecto_appmovil.data.local.entity.UsuarioEntity
import com.example.proyecto_appmovil.data.remote.dto.AspiranteDto
import com.example.proyecto_appmovil.data.remote.dto.DocumentoItemDto
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.model.TipoDocumento

internal fun AspiranteDto.toEntity() = AspiranteEntity(
    idAspirante = idAspirante,
    idUsuario = idUsuario,
    folio = folio,
    curp = curp,
    telefono = telefono.orEmpty(),
    direccion = direccion.orEmpty(),
    idCarrera = idCarrera,
    carreraNombre = carreraNombre.orEmpty(),
    estadoResidencia = estadoResidencia.orEmpty(),
    municipioResidencia = municipioResidencia.orEmpty(),
    promedioBachillerato = promedioBachillerato,
    estadoInscripcion = estadoInscripcion
)

internal fun AspiranteDto.toModel() = toEntity().toModel(nombreCompleto, correo, "aspirante")

internal fun AspiranteEntity.toModel(usuario: UsuarioEntity) =
    toModel("${usuario.nombre} ${usuario.apellidos}".trim(), usuario.correo, usuario.rol)

private fun AspiranteEntity.toModel(nombreCompleto: String, correo: String, rol: String) = Aspirante(
    id = idAspirante.toString(),
    nombreCompleto = nombreCompleto,
    curp = curp,
    email = correo,
    telefono = telefono,
    carrera = carreraNombre,
    folioAspirante = folio,
    promedioBachillerato = promedioBachillerato,
    direccion = direccion,
    estadoResidencia = estadoResidencia,
    municipioResidencia = municipioResidencia,
    estadoInscripcion = estadoInscripcion.toEstadoInscripcion(),
    rol = rol
)

private fun String.toEstadoInscripcion(): EstadoInscripcion = when (lowercase()) {
    "en_captura", "pendiente" -> EstadoInscripcion.PENDIENTE
    "en_revision" -> EstadoInscripcion.EN_REVISION
    "con_observaciones" -> EstadoInscripcion.CON_OBSERVACIONES
    "documentacion_aprobada", "aceptado" -> EstadoInscripcion.ACEPTADO
    "rechazado" -> EstadoInscripcion.RECHAZADO
    else -> error("Estado de inscripción desconocido: $this")
}

internal fun DocumentoItemDto.toEntity() = DocumentoEntity(
    idLocal = id,
    idRemoto = id,
    idAspirante = idAspirante,
    idTipoDocumento = idTipoDocumento,
    tipoCodigo = tipoCodigo,
    nombreOriginal = nombreOriginal,
    tamanoBytes = tamanoBytes,
    mimeType = mimeType,
    version = version,
    estado = estado,
    observaciones = observaciones,
    fechaSubida = fechaSubida
)

internal fun List<DocumentoEntity>.completeSlots(): List<Documento> {
    val byType = associateBy { it.tipoCodigo }
    return TipoDocumento.entries.map { tipo ->
        byType[tipo.name]?.toModel(tipo) ?: Documento(id = "", tipo = tipo)
    }
}

private fun DocumentoEntity.toModel(tipo: TipoDocumento) = Documento(
    id = (idRemoto ?: idLocal).toString(),
    tipo = tipo,
    nombreArchivo = nombreOriginal,
    tamanoBytes = tamanoBytes,
    fechaSubida = fechaSubida,
    estado = when (estado.lowercase()) {
        "aprobado", "validado" -> EstadoDocumento.VALIDADO
        "requiere_correccion", "rechazado", "presencial" -> EstadoDocumento.RECHAZADO
        "pendiente" -> EstadoDocumento.PENDIENTE
        "subido" -> EstadoDocumento.SUBIDO
        else -> error("Estado de documento desconocido: $estado")
    },
    observaciones = observaciones.orEmpty(),
    version = version
)
