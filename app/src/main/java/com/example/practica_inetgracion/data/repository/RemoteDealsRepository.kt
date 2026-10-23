package com.example.practica_inetgracion.data.repository

import com.example.practica_inetgracion.data.model.ProductOffer
import com.example.practica_inetgracion.data.remote.ApiErrorResponse
import com.example.practica_inetgracion.data.remote.SearchProductsRequest
import com.example.practica_inetgracion.data.remote.TinyPriceApi
import com.example.practica_inetgracion.data.remote.TinyPriceApiFactory
import com.example.practica_inetgracion.data.remote.toProductOffer
import com.example.practica_inetgracion.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RemoteDealsRepository(
    private val api: TinyPriceApi = TinyPriceApiFactory.create(),
    private val accessTokenProvider: suspend () -> String = { throw RemoteSearchException("Inicia sesión para buscar ofertas.") },
    private val apiKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
    private val gson: Gson = Gson(),
) : DealsRepository {
    override val isPreviewData = false

    override suspend fun search(query: String): List<ProductOffer> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchProducts(apiKey, "Bearer ${accessTokenProvider()}", SearchProductsRequest(query))
            if (!response.isSuccessful) throw RemoteSearchException(response.errorMessage())
            response.body()?.offers.orEmpty().map { it.toProductOffer() }
        } catch (exception: RemoteSearchException) {
            throw exception
        } catch (_: Exception) {
            throw RemoteSearchException("No se pudo conectar con la búsqueda en vivo. Revisa tu conexión e inténtalo de nuevo.")
        }
    }

    private fun Response<*>.errorMessage(): String = runCatching {
        errorBody()?.charStream()?.use { gson.fromJson(it, ApiErrorResponse::class.java)?.error }
    }.getOrNull().orEmpty().ifBlank { "No fue posible hacer la búsqueda en vivo. Inténtalo de nuevo." }
}

class RemoteSearchException(message: String) : Exception(message)
