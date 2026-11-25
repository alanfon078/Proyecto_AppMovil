package com.example.proyecto_appmovil.data.remote

import com.example.proyecto_appmovil.BuildConfig
import com.example.proyecto_appmovil.data.local.session.SessionManager
import com.example.proyecto_appmovil.data.local.session.SessionTokens
import com.example.proyecto_appmovil.data.remote.api.InscripcionesApiService
import com.example.proyecto_appmovil.data.remote.dto.RefreshTokenRequestDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder().header("Accept", "application/json")
        val path = original.url.encodedPath
        if (publicPaths.none(path::endsWith)) {
            tokenProvider()?.let { request.header("Authorization", "Bearer $it") }
        }
        return chain.proceed(request.build())
    }

    private companion object {
        val publicPaths = listOf("/auth/login", "/auth/register", "/auth/refresh", "/catalogos/carreras")
    }
}

object RetrofitClient {
    const val BASE_URL = BuildConfig.BASE_URL

    fun createOkHttpClient(tokenProvider: () -> String? = { null }): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
                else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(logging)
            .build()
    }

    fun createApiService(sessionManager: SessionManager? = null): InscripcionesApiService {
        val client = createOkHttpClient { sessionManager?.getTokens()?.accessToken }
        if (sessionManager == null) return createApi(client)

        val refreshApi = createApi(createOkHttpClient())
        val authenticated = client.newBuilder().authenticator { _, response ->
            val token = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (response.priorResponse?.code == 401 ||
                token == null ||
                response.header("WWW-Authenticate")?.startsWith("Bearer", ignoreCase = true) != true
            ) {
                return@authenticator null
            }

            val renewed = sessionManager.refreshTokens(token) { refreshToken ->
                val result = refreshApi.refresh(RefreshTokenRequestDto(refreshToken)).execute()
                result.errorBody()?.close()
                result.body()?.let { SessionTokens(it.accessToken, it.refreshToken) }
            } ?: return@authenticator null

            val retry = response.request.newBuilder().header("Authorization", "Bearer ${renewed.accessToken}")
            if (response.request.url.encodedPath.endsWith("/auth/logout")) {
                val body = Gson().toJson(RefreshTokenRequestDto(renewed.refreshToken))
                retry.post(body.toRequestBody("application/json".toMediaType()))
            }
            retry.build()
        }.build()
        return createApi(authenticated)
    }

    private fun createApi(client: OkHttpClient): InscripcionesApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(InscripcionesApiService::class.java)
}
