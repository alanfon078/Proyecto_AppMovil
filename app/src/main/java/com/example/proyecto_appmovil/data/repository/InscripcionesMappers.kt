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
import com.example.proyecto_appmovil.data.remote.dto.PreguntaDto
import com.example.proyecto_appmovil.data.local.entity.PreguntaDudaEntity
import com.example.proyecto_appmovil.model.PreguntaDuda
import com.example.proyecto_appmovil.model.CategoriaPregunta
import com.example.proyecto_appmovil.model.EstadoPregunta
import com.example.proyecto_appmovil.data.local.entity.FechaImportanteEntity
import com.example.proyecto_appmovil.data.local.entity.NotificacionEntity
import com.example.proyecto_appmovil.data.remote.dto.FechaImportanteDto
import com.example.proyecto_appmovil.data.remote.dto.NotificacionItemDto
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
    idLocal = requireNotNull(id),
    idRemoto = id,
    idAspirante = idAspirante,
    idTipoDocumento = idTipoDocumento,
    tipoCodigo = tipoCodigo,
    nombreOriginal = nombreOriginal,
    tamanoBytes = requireNotNull(tamanoBytes),
    mimeType = mimeType,
    version = requireNotNull(version),
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
        "pendiente", "subido" -> EstadoDocumento.SUBIDO
        else -> error("Estado de documento desconocido: $estado")
    },
    observaciones = observaciones.orEmpty(),
    version = version
)

internal fun PreguntaDto.toEntity() = PreguntaDudaEntity(
    idPregunta = idPregunta,
    nombreCompleto = nombreCompleto.orEmpty(),
    folio = folio.orEmpty(),
    carrera = carrera.orEmpty(),
    categoria = categoria,
    pregunta = pregunta,
    estado = estado,
    respuesta = respuesta,
    fechaCreacion = fechaCreacion,
    fechaRespuesta = fechaRespuesta
)

internal fun PreguntaDto.toModel() = toEntity().toModel()

internal fun NotificacionItemDto.toEntity() = NotificacionEntity(
    idNotificacion = id, idRemoto = id, idUsuario = idUsuario, tipo = tipo,
    titulo = titulo, mensaje = mensaje, leida = leida, fechaEnvio = fechaEnvio, idPregunta = idPregunta
)

internal fun NotificacionEntity.toModel() =
    Notificacion((idRemoto ?: idNotificacion).toString(), titulo, mensaje, fechaEnvio, leida, tipo)

internal fun FechaImportanteDto.toEntity() =
    FechaImportanteEntity(idFecha, titulo, descripcion.orEmpty(), tipo, fechaLimite, idCarrera, activa)

internal fun FechaImportanteEntity.toModel() = FechaImportante(
    idFecha.toString(), titulo, descripcion, fechaLimite,
    ChronoUnit.DAYS.between(
        LocalDate.now(ZoneId.of("America/Mexico_City")), LocalDate.parse(fechaLimite.take(10))
    ).toInt(),
    tipo
)

internal fun PreguntaDudaEntity.toModel() = PreguntaDuda(
    id = idPregunta.toString(),
    nombreCompleto = nombreCompleto,
    folio = folio,
    carrera = carrera,
    categoria = CategoriaPregunta.fromApi(categoria) ?: CategoriaPregunta.FECHAS,
    pregunta = pregunta,
    estado = EstadoPregunta.fromApi(estado),
    respuesta = respuesta,
    fechaCreacion = fechaCreacion,
    fechaRespuesta = fechaRespuesta
)
