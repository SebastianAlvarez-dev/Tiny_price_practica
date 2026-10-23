package com.example.practica_inetgracion.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TinyPriceApi {
    @POST("search-products")
    suspend fun searchProducts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearer: String,
        @Body request: SearchProductsRequest,
    ): Response<SearchProductsResponse>
}
