package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.model.*
import kotlinx.coroutines.delay

class InscripcionesRepositoryImpl : InscripcionesRepository {

    private var currentAspirante = Aspirante(
        id = "1",
        nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
        curp = "OOAD030515HGTRRN01",
        email = "aspirante@itsur.edu.mx",
        telefono = "4451234567",
        carrera = "Ing. Sistemas Computacionales",
        folioAspirante = "ITSUR-2026-0042",
        promedioBachillerato = 9.2,
        direccion = "Calle Hidalgo #123, Uriangato, Gto.",
        estadoInscripcion = EstadoInscripcion.EN_REVISION,
        rol = "aspirante"
    )

    private val documentosList = mutableListOf(
        Documento(
            id = "doc_1",
            tipo = TipoDocumento.ACTA_NACIMIENTO,
            nombreArchivo = "Acta_Nacimiento_Dafne.pdf",
            uriString = "content://media/external/file/101",
            tamanoBytes = 1420500,
            fechaSubida = "01/11/2026 10:30",
            estado = EstadoDocumento.VALIDADO,
            observaciones = "Documento verificado correctamente."
        ),
        Documento(
            id = "doc_2",
            tipo = TipoDocumento.CURP,
            nombreArchivo = "CURP_Certificada.pdf",
            uriString = "content://media/external/file/102",
            tamanoBytes = 850200,
            fechaSubida = "01/11/2026 10:32",
            estado = EstadoDocumento.VALIDADO,
            observaciones = "CURP actualizada ante RENAPO."
        ),
        Documento(
            id = "doc_3",
            tipo = TipoDocumento.CERTIFICADO_BACHILLERATO,
            nombreArchivo = "Certificado_Preparatoria.pdf",
            uriString = "content://media/external/file/103",
            tamanoBytes = 2950000,
            fechaSubida = "02/11/2026 14:15",
            estado = EstadoDocumento.SUBIDO,
            observaciones = "Pendiente de cotejo por Servicios Escolares."
        ),
        Documento(
            id = "doc_4",
            tipo = TipoDocumento.COMPROBANTE_DOMICILIO,
            nombreArchivo = null,
            uriString = null,
            tamanoBytes = 0,
            fechaSubida = null,
            estado = EstadoDocumento.PENDIENTE,
            observaciones = "Recibo de luz, agua o teléfono no mayor a 3 meses."
        )
    )

    private val notificacionesList = mutableListOf(
        Notificacion(
            id = "notif_1",
            titulo = "Documentos en Revisión",
            mensaje = "Tus documentos han sido recibidos por el personal de Servicios Escolares del ITSUR y están en proceso de verificación.",
            fecha = "02 de Noviembre, 2026",
            leida = false
        ),
        Notificacion(
            id = "notif_2",
            titulo = "Examen Diagnóstico de Admisión",
            mensaje = "El examen diagnóstico de nuevo ingreso se aplicará en línea el día 15 de Noviembre a las 09:00 hrs.",
            fecha = "30 de Octubre, 2026",
            leida = false
        ),
        Notificacion(
            id = "notif_3",
            titulo = "Registro Exitoso",
            mensaje = "Bienvenido al proceso de admisión ITSUR 2026. Tu número de folio asignado es ITSUR-2026-0042.",
            fecha = "28 de Octubre, 2026",
            leida = true
        )
    )

    private val fechasList = listOf(
        FechaImportante(
            id = "f_1",
            titulo = "Cierre de Carga de Documentos",
            descripcion = "Fecha límite para subir Acta, CURP y Certificado al sistema.",
            fechaLimite = "10 de Noviembre, 2026",
            diasRestantes = 8,
            tipo = "limite_entrega"
        ),
        FechaImportante(
            id = "f_2",
            titulo = "Examen de Admisión / Diagnóstico",
            descripcion = "Aplicación del examen CENEVAL en instalaciones ITSUR y virtual.",
            fechaLimite = "15 de Noviembre, 2026",
            diasRestantes = 13,
            tipo = "examen"
        ),
        FechaImportante(
            id = "f_3",
            titulo = "Publicación de Resultados",
            descripcion = "Consulta de listas de aspirantes aceptados en el portal oficial.",
            fechaLimite = "25 de Noviembre, 2026",
            diasRestantes = 23,
            tipo = "publicacion"
        ),
        FechaImportante(
            id = "f_4",
            titulo = "Pago de Cuota de Inscripción",
            descripcion = "Pago bancario de la cuota semestral de nuevo ingreso.",
            fechaLimite = "05 de Diciembre, 2026",
            diasRestantes = 33,
            tipo = "pago"
        )
    )

    private var comprobantePago = ComprobantePago(
        id = "pago_1",
        aspiranteId = "1",
        folio = "BBVA-84920193",
        monto = 2850.0,
        fechaPago = "02/11/2026",
        comprobanteUri = "",
        validado = false
    )

    private var reglamentoFirmado = false

    private val aspirantesAdminList = mutableListOf(
        Aspirante(
            id = "1",
            nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
            curp = "OOAD030515HGTRRN01",
            email = "aspirante@itsur.edu.mx",
            telefono = "4451234567",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0042",
            promedioBachillerato = 9.2,
            direccion = "Uriangato, Gto.",
            estadoInscripcion = EstadoInscripcion.EN_REVISION,
            rol = "aspirante"
        ),
        Aspirante(
            id = "2",
            nombreCompleto = "Alan Yael Fonseca Ruiz",
            curp = "FORA020820HGTRNA09",
            email = "alan@itsur.edu.mx",
            telefono = "4457654321",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0043",
            promedioBachillerato = 9.5,
            direccion = "Moroleón, Gto.",
            estadoInscripcion = EstadoInscripcion.ACEPTADO,
            rol = "aspirante"
        ),
        Aspirante(
            id = "3",
            nombreCompleto = "Montserrat Tenorio Villagómez",
            curp = "TEVM031110MGTSRR05",
            email = "montserrat@itsur.edu.mx",
            telefono = "4459876543",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0044",
            promedioBachillerato = 9.0,
            direccion = "Yuriria, Gto.",
            estadoInscripcion = EstadoInscripcion.CON_OBSERVACIONES,
            rol = "aspirante"
        )
    )

    override suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante> {
        delay(400) // Simulación breve de red
        val userTrim = usuarioOCorreo.trim().lowercase()

        // Si el usuario es escolar o admin, entra al rol de Control Escolar
        if (userTrim.contains("admin") || userTrim.contains("escolar") || userTrim.contains("control")) {
            val adminUser = currentAspirante.copy(
                nombreCompleto = "Lic. Gustavo Vega (Control Escolar)",
                email = "escolar@itsur.edu.mx",
                rol = "escolar"
            )
            return Result.success(adminUser)
        }

        return Result.success(currentAspirante)
    }

    override suspend fun registrarAspirante(
        nombreCompleto: String,
        curp: String,
        email: String,
        telefono: String,
        carrera: String,
        direccion: String,
        contrasena: String
    ): Result<Boolean> {
        delay(500)
        currentAspirante = currentAspirante.copy(
            nombreCompleto = nombreCompleto,
            curp = curp.uppercase(),
            email = email,
            telefono = telefono,
            carrera = carrera,
            direccion = direccion,
            folioAspirante = "ITSUR-2026-" + (100..999).random(),
            estadoInscripcion = EstadoInscripcion.PENDIENTE
        )
        return Result.success(true)
    }

    override suspend fun getAspiranteActual(): Aspirante {
        return currentAspirante
    }

    override suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean> {
        delay(300)
        currentAspirante = aspirante
        return Result.success(true)
    }

    override suspend fun getDocumentos(): List<Documento> {
        return documentosList.toList()
    }

    override suspend fun subirDocumento(
        tipo: TipoDocumento,
        nombreArchivo: String,
        uriString: String,
        tamanoBytes: Long
    ): Result<Boolean> {
        delay(600)
        val index = documentosList.indexOfFirst { it.tipo == tipo }
        val nuevoDoc = Documento(
            id = "doc_${tipo.name.lowercase()}",
            tipo = tipo,
            nombreArchivo = nombreArchivo,
            uriString = uriString,
            tamanoBytes = tamanoBytes,
            fechaSubida = "Hoy",
            estado = EstadoDocumento.SUBIDO,
            observaciones = "Documento cargado correctamente, en espera de revisión."
        )
        if (index != -1) {
            documentosList[index] = nuevoDoc
        } else {
            documentosList.add(nuevoDoc)
        }
        return Result.success(true)
    }

    override suspend fun getNotificaciones(): List<Notificacion> {
        return notificacionesList.toList()
    }

    override suspend fun marcarNotificacionLeida(id: String) {
        val index = notificacionesList.indexOfFirst { it.id == id }
        if (index != -1) {
            notificacionesList[index] = notificacionesList[index].copy(leida = true)
        }
    }

    override suspend fun getFechasImportantes(): List<FechaImportante> {
        return fechasList
    }

    override suspend fun getComprobantePago(): ComprobantePago {
        return comprobantePago
    }

    override suspend fun registrarComprobantePago(
        folio: String,
        monto: Double,
        fechaPago: String,
        comprobanteUri: String
    ): Result<Boolean> {
        delay(400)
        comprobantePago = comprobantePago.copy(
            folio = folio,
            monto = monto,
            fechaPago = fechaPago,
            comprobanteUri = comprobanteUri,
            validado = false
        )
        return Result.success(true)
    }

    override suspend fun isReglamentoFirmado(): Boolean {
        return reglamentoFirmado
    }

    override suspend fun firmarReglamento(firmaData: String): Result<Boolean> {
        delay(300)
        reglamentoFirmado = true
        return Result.success(true)
    }

    override suspend fun getAspirantesParaRevision(): List<Aspirante> {
        return aspirantesAdminList.toList()
    }

    override suspend fun getAspirantePorId(id: String): Aspirante? {
        return aspirantesAdminList.find { it.id == id } ?: currentAspirante
    }

    override suspend fun revisarDocumento(
        documentoId: String,
        aprobado: Boolean,
        observaciones: String
    ): Result<Boolean> {
        delay(400)
        val doc = documentosList.find { it.id == documentoId }
        if (doc != null) {
            val nuevoEstado = if (aprobado) EstadoDocumento.VALIDADO else EstadoDocumento.RECHAZADO
            val index = documentosList.indexOf(doc)
            documentosList[index] = doc.copy(estado = nuevoEstado, observaciones = observaciones)
        }
        return Result.success(true)
    }
}
