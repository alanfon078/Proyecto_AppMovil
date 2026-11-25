package com.example.proyecto_appmovil

import com.example.proyecto_appmovil.data.local.entity.*
import com.example.proyecto_appmovil.data.remote.RetrofitClient
import com.example.proyecto_appmovil.data.remote.api.InscripcionesApiService
import com.example.proyecto_appmovil.data.remote.dto.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class DataLayerArchitectureTest {

    private val gson = Gson()

    @Test
    fun retrofitClient_configurationIsCorrect() {
        assertEquals(BuildConfig.BASE_URL, RetrofitClient.BASE_URL)
        val okHttpClient = RetrofitClient.createOkHttpClient { "mock_jwt_token" }
        assertNotNull(okHttpClient)
        assertEquals(30_000, okHttpClient.connectTimeoutMillis)
        assertEquals(30_000, okHttpClient.readTimeoutMillis)
        assertEquals(30_000, okHttpClient.writeTimeoutMillis)

        val apiService = RetrofitClient.createApiService()
        assertNotNull(apiService)
        assertTrue(InscripcionesApiService::class.java.isInterface)
    }

    @Test
    fun dtoSerialization_loginRequestAndResponse_serializesCorrectly() {
        val loginRequest = LoginRequestDto(
            identificador = "aspirante@itsur.edu.mx",
            contrasena = "Secret123"
        )
        val json = gson.toJson(loginRequest)
        assertTrue(json.contains("\"identificador\":\"aspirante@itsur.edu.mx\""))
        assertTrue(json.contains("\"contrasena\":\"Secret123\""))

        val mockResponseJson = """
            {
                "access_token": "test_access_token",
                "refresh_token": "test_refresh_token",
                "usuario": {
                    "id_usuario": 100,
                    "nombre": "Montserrat",
                    "apellidos": "Tenorio",
                    "nombre_usuario": "mtenorio",
                    "correo": "mtenorio@itsur.edu.mx",
                    "rol": "aspirante",
                    "activo": true
                },
                "aspirante": {
                    "id_aspirante": 50,
                    "id_usuario": 100,
                    "folio": "FOL-2026-001",
                    "nombre_completo": "Montserrat Tenorio",
                    "correo": "mtenorio@itsur.edu.mx",
                    "estado_residencia": "Jalisco",
                    "municipio_residencia": "Jalostotitlán",
                    "curp": "TEMM980101HGTNRA01",
                    "telefono": "4451234567",
                    "direccion": "Av. Tecnológico #1",
                    "promedio_bachillerato": 9.2,
                    "estado_inscripcion": "en_captura"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(mockResponseJson, LoginResponseDto::class.java)
        assertEquals("test_access_token", response.accessToken)
        assertEquals("test_refresh_token", response.refreshToken)
        assertEquals(100L, response.usuario.idUsuario)
        assertEquals("Montserrat", response.usuario.nombre)
        assertEquals("Montserrat Tenorio", response.aspirante?.nombreCompleto)
        assertEquals("Jalisco", response.aspirante?.estadoResidencia)
        assertEquals("Jalostotitlán", response.aspirante?.municipioResidencia)
        assertEquals("en_captura", response.aspirante?.estadoInscripcion)
        assertEquals(9.2, response.aspirante?.promedioBachillerato ?: 0.0, 0.01)
    }

    @Test
    fun documentResponses_shareTheServerPayload() {
        val document = """
            {
                "id_documento": 500,
                "id_aspirante": 10,
                "id_tipo_documento": 1,
                "tipo_codigo": "CURP",
                "nombre_original": "curp.pdf",
                "tamano_bytes": 567,
                "tipo_mime": "application/pdf",
                "version": 2,
                "fecha_subida": "2026-11-24 10:00:00",
                "estado": "pendiente",
                "observaciones": null
            }
        """.trimIndent()
        val upload = gson.fromJson(
            """{"documento": $document, "estado_inscripcion": "en_revision"}""",
            SubirDocumentoResponseDto::class.java
        )
        assertEquals("curp.pdf", upload.documento.nombreOriginal)
        assertEquals(567L, upload.documento.tamanoBytes)
        assertEquals("en_revision", upload.estadoInscripcion)

        val dictamen = gson.fromJson(
            """{"id_revision": 5, "resultado": "aprobado", "documento": $document, "estado_inscripcion": "en_revision"}""",
            RevisionResponseDto::class.java
        )
        assertEquals(500L, dictamen.documento.id)
        assertEquals("aprobado", dictamen.resultado)
        assertEquals("en_revision", dictamen.estadoInscripcion)
    }

    @Test
    fun entities_creationAndProperties_areValid() {
        val usuario = UsuarioEntity(
            idUsuario = 1L,
            nombre = "Carlos",
            apellidos = "Ramírez",
            nombreUsuario = "carlosr",
            correo = "carlos@itsur.edu.mx",
            rol = "ASPIRANTE"
        )
        assertEquals(1L, usuario.idUsuario)
        assertTrue(usuario.activo)

        val aspirante = AspiranteEntity(
            idAspirante = 10L,
            idUsuario = 1L,
            folio = "FOL-2026-100",
            curp = "CURP12345678901234",
            telefono = "4459998877",
            direccion = "Calle Hidalgo 123",
            carreraNombre = "Ingeniería en Gestión Empresarial",
            estadoResidencia = "Querétaro",
            municipioResidencia = "Amealco de Bonfil",
            promedioBachillerato = 8.9,
            estadoInscripcion = "EN_REVISION"
        )
        assertEquals(10L, aspirante.idAspirante)
        assertEquals(1L, aspirante.idUsuario)
        assertEquals("Amealco de Bonfil", aspirante.municipioResidencia)

        val documento = DocumentoEntity(
            idLocal = 1L,
            idRemoto = 500L,
            idAspirante = 10L,
            idTipoDocumento = 1,
            tipoCodigo = "ACTA_NACIMIENTO",
            nombreOriginal = "acta_nacimiento.pdf",
            tamanoBytes = 1024L * 500,
            mimeType = "application/pdf",
            estado = "pendiente"
        )
        assertEquals("ACTA_NACIMIENTO", documento.tipoCodigo)
        assertEquals("acta_nacimiento.pdf", documento.nombreOriginal)

        val notificacion = NotificacionEntity(
            idNotificacion = 1L,
            idRemoto = 300L,
            idUsuario = 1L,
            tipo = "DOCUMENTO",
            titulo = "Documento Validado",
            mensaje = "Tu acta de nacimiento fue aceptada.",
            leida = false,
            fechaEnvio = "2026-08-16T10:00:00"
        )
        assertFalse(notificacion.leida)

        val fecha = FechaImportanteEntity(
            idFecha = 1,
            titulo = "Cierre de Carga de Documentos",
            descripcion = "Fecha límite para adjuntar documentos digitales.",
            tipo = "DOCUMENTOS",
            fechaLimite = "2026-08-30",
            activa = true
        )
        assertTrue(fecha.activa)
    }
}
