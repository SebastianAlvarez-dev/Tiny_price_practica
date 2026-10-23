package com.example.practica_inetgracion.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class AuthCredentials(val email: String, val password: String)
data class RefreshTokenRequest(val refresh_token: String)
data class AuthResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_in: Long = 3600,
)
data class AuthErrorResponse(
    val msg: String? = null,
    val message: String? = null,
    val error_description: String? = null,
)

interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(@Header("apikey") apiKey: String, @Body credentials: AuthCredentials): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Header("apikey") apiKey: String, @Body credentials: AuthCredentials): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refresh(
        @Header("apikey") apiKey: String,
        @Body request: RefreshTokenRequest,
    ): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun signOut(@Header("apikey") apiKey: String, @Header("Authorization") bearer: String): Response<Unit>
}
