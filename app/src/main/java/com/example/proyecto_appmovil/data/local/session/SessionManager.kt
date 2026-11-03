package com.example.proyecto_appmovil.data.local.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("fallback_session_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_ASPIRANTE_ID = "key_aspirante_id"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_EMAIL = "key_user_email"
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveSession(
        token: String,
        refreshToken: String? = null,
        userId: Long,
        aspiranteId: Long? = null,
        role: String,
        email: String
    ) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_USER_ID, userId)
            .putLong(KEY_ASPIRANTE_ID, aspiranteId ?: 0L)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserId(): Long = sharedPreferences.getLong(KEY_USER_ID, 0L)
    fun getAspiranteId(): Long = sharedPreferences.getLong(KEY_ASPIRANTE_ID, 0L)
    fun getUserRole(): String? = sharedPreferences.getString(KEY_USER_ROLE, null)
    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }
}
