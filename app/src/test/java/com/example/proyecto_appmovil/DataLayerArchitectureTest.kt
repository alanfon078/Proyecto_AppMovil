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
        assertEquals("http://10.0.2.2:3000/api/v1/", RetrofitClient.BASE_URL)
        val okHttpClient = RetrofitClient.createOkHttpClient { "mock_jwt_token" }
        assertNotNull(okHttpClient)
        assertEquals(30_000, okHttpClient.connectTimeoutMillis)
        assertEquals(30_000, okHttpClient.readTimeoutMillis)
        assertEquals(30_000, okHttpClient.writeTimeoutMillis)

        val apiService = RetrofitClient.createApiService { "mock_jwt_token" }
        assertNotNull(apiService)
        assertTrue(InscripcionesApiService::class.java.isInterface)
    }

    @Test
    fun dtoSerialization_loginRequestAndResponse_serializesCorrectly() {
        val loginRequest = LoginRequestDto(
            email = "aspirante@itsur.edu.mx",
            contrasena = "Secret123"
        )
        val json = gson.toJson(loginRequest)
        assertTrue(json.contains("\"email\":\"aspirante@itsur.edu.mx\""))
        assertTrue(json.contains("\"contrasena\":\"Secret123\""))

        val mockResponseJson = """
            {
                "token": "fake_jwt_token",
                "usuario": {
                    "idUsuario": 100,
                    "nombre": "Montserrat",
                    "apellidos": "Tenorio",
                    "nombreUsuario": "mtenorio",
                    "correo": "mtenorio@itsur.edu.mx",
                    "rol": "ASPIRANTE",
                    "activo": true
                },
                "aspirante": {
                    "idAspirante": 50,
                    "idUsuario": 100,
                    "folio": "FOL-2026-001",
                    "curp": "TEMM980101HGTNRA01",
                    "telefono": "4451234567",
                    "direccion": "Av. Tecnológico #1",
                    "promedioBachillerato": 9.2,
                    "estadoInscripcion": "PENDIENTE"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(mockResponseJson, LoginResponseDto::class.java)
        assertEquals("fake_jwt_token", response.token)
        assertEquals(100L, response.usuario.idUsuario)
        assertEquals("Montserrat", response.usuario.nombre)
        assertEquals("FOL-2026-001", response.aspirante?.folio)
        assertEquals(9.2, response.aspirante?.promedioBachillerato ?: 0.0, 0.01)
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
            promedioBachillerato = 8.9,
            estadoInscripcion = "EN_REVISION"
        )
        assertEquals(10L, aspirante.idAspirante)
        assertEquals(1L, aspirante.idUsuario)

        val documento = DocumentoEntity(
            idLocal = 1L,
            idRemoto = 500L,
            idAspirante = 10L,
            idTipoDocumento = 1,
            tipoCodigo = "ACTA_NACIMIENTO",
            rutaLocal = "/storage/emulated/0/Download/acta.pdf",
            nombreOriginal = "acta_nacimiento.pdf",
            tamanoBytes = 1024L * 500,
            mimeType = "application/pdf",
            estado = "SUBIDO",
            syncPendiente = false
        )
        assertEquals("ACTA_NACIMIENTO", documento.tipoCodigo)
        assertFalse(documento.syncPendiente)

        val pago = ComprobantePagoEntity(
            idPago = 1L,
            idRemoto = 200L,
            idAspirante = 10L,
            folioPago = "FICH-998877",
            monto = 1500.0,
            fechaPago = "2026-08-15",
            rutaComprobanteLocal = "/images/pago.jpg",
            estado = "SUBIDO"
        )
        assertEquals(1500.0, pago.monto, 0.01)

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
