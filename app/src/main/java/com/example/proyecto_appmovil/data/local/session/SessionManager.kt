package com.example.proyecto_appmovil.data.local.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class SessionTokens(val accessToken: String, val refreshToken: String)

class SessionManager(context: Context) {
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_session_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        context.deleteSharedPreferences("fallback_session_prefs")
    }

    @Synchronized
    fun getTokens(): SessionTokens? {
        val accessToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) return null
        return SessionTokens(accessToken, refreshToken)
    }

    @Synchronized
    fun saveSession(tokens: SessionTokens, userId: Long, aspiranteId: Long?, role: String, email: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_USER_ID, userId)
            .putLong(KEY_ASPIRANTE_ID, aspiranteId ?: 0L)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    @Synchronized
    fun refreshTokens(rejectedAccessToken: String, refresh: (String) -> SessionTokens?): SessionTokens? {
        val current = getTokens() ?: return null
        if (current.accessToken != rejectedAccessToken) return current
        val tokens = refresh(current.refreshToken) ?: return null
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .apply()
        return tokens
    }

    fun getUserId(): Long = sharedPreferences.getLong(KEY_USER_ID, 0L)
    fun getAspiranteId(): Long = sharedPreferences.getLong(KEY_ASPIRANTE_ID, 0L)
    fun getUserRole(): String? = sharedPreferences.getString(KEY_USER_ROLE, null)

    @Synchronized
    fun getUltimoIdNotificado(): Long = sharedPreferences.getLong(KEY_ULTIMO_ID_NOTIFICADO, 0L)

    @Synchronized
    fun saveUltimoIdNotificado(id: Long) {
        if (id > getUltimoIdNotificado()) {
            sharedPreferences.edit().putLong(KEY_ULTIMO_ID_NOTIFICADO, id).apply()
        }
    }

    @Synchronized
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getTokens() != null

    private companion object {
        const val KEY_AUTH_TOKEN = "key_auth_token"
        const val KEY_REFRESH_TOKEN = "key_refresh_token"
        const val KEY_USER_ID = "key_user_id"
        const val KEY_ASPIRANTE_ID = "key_aspirante_id"
        const val KEY_USER_ROLE = "key_user_role"
        const val KEY_USER_EMAIL = "key_user_email"
        const val KEY_ULTIMO_ID_NOTIFICADO = "key_ultimo_id_notificado"
    }
}
