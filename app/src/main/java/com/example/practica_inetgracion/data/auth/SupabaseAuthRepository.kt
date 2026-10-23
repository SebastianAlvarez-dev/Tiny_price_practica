package com.example.practica_inetgracion.data.auth

import com.example.practica_inetgracion.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
)

data class AuthResult(val session: AuthSession?, val message: String? = null)

class SupabaseAuthRepository(
    private val api: SupabaseAuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SupabaseAuthApi::class.java),
    private val apiKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
    private val gson: Gson = Gson(),
) {
    private val refreshMutex = Mutex()
    // shortcut: sessions are memory-only; add encrypted persistence if users must stay signed in after app restart.
    @Volatile private var session: AuthSession? = null

    suspend fun signUp(email: String, password: String): AuthResult {
        val response = api.signUp(apiKey, AuthCredentials(email.trim(), password))
        if (!response.isSuccessful) throw AuthException(response.errorMessage())
        val body = response.body() ?: throw AuthException("Supabase no devolvió una respuesta válida.")
        return body.toSession()?.also { session = it }?.let { AuthResult(it) }
            ?: AuthResult(null, "Cuenta creada. Confirma tu correo y después inicia sesión.")
    }

    suspend fun signIn(email: String, password: String): AuthSession {
        val response = api.signIn(apiKey, AuthCredentials(email.trim(), password))
        if (!response.isSuccessful) throw AuthException(response.errorMessage())
        return (response.body()?.toSession() ?: throw AuthException("No se recibió una sesión válida."))
            .also { session = it }
    }

    suspend fun validAccessToken(): String = refreshMutex.withLock {
        val current = session ?: throw AuthException("Inicia sesión para buscar ofertas.")
        if (current.expiresAtMillis > System.currentTimeMillis() + 60_000) return@withLock current.accessToken
        val response = api.refresh(apiKey, RefreshTokenRequest(current.refreshToken))
        if (!response.isSuccessful) {
            session = null
            throw AuthException("Tu sesión venció. Inicia sesión otra vez.")
        }
        val refreshed = response.body()?.toSession()
            ?: throw AuthException("No se pudo renovar la sesión. Inicia sesión otra vez.")
        session = refreshed
        refreshed.accessToken
    }

    suspend fun signOut() {
        session?.let { runCatching { api.signOut(apiKey, "Bearer ${it.accessToken}") } }
        session = null
    }

    private fun AuthResponse.toSession(): AuthSession? {
        val access = access_token ?: return null
        val refresh = refresh_token ?: return null
        return AuthSession(access, refresh, System.currentTimeMillis() + expires_in * 1000)
    }

    private fun retrofit2.Response<*>.errorMessage(): String = runCatching {
        errorBody()?.charStream()?.use { reader ->
            val error = gson.fromJson(reader, AuthErrorResponse::class.java)
            error.msg ?: error.message ?: error.error_description
        }
    }.getOrNull().orEmpty().ifBlank { "No se pudo autenticar. Revisa el correo, la contraseña y la conexión." }

}

class AuthException(message: String) : Exception(message)
