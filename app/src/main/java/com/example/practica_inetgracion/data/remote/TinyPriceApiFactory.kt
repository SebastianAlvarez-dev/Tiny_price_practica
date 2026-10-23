package com.example.practica_inetgracion.data.remote

import com.example.practica_inetgracion.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TinyPriceApiFactory {
    fun create(): TinyPriceApi = Retrofit.Builder()
        .baseUrl(BuildConfig.EDGE_RUNTIME_BASE_URL.ensureTrailingSlash())
        .client(OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TinyPriceApi::class.java)

    private fun String.ensureTrailingSlash() = if (endsWith('/')) this else "$this/"
}
