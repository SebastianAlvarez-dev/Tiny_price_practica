package com.example.practica_inetgracion.data.remote

import com.example.practica_inetgracion.data.model.ProductOffer

data class SearchProductsRequest(val query: String)
data class SearchProductsResponse(val offers: List<ProductOfferDto> = emptyList())
data class ProductOfferDto(
    val id: String = "",
    val store: String = "Fuente web",
    val productName: String = "",
    val price: String = "Ver en fuente",
    val originalPrice: String? = null,
    val shipping: String = "Consulta en tienda",
    val delivery: String = "Disponibilidad por confirmar",
    val rating: String = "—",
    val sourceUrl: String = "",
    val summary: String = "Resultado encontrado en la web.",
    val tags: List<String> = listOf("Resultado web"),
    val isBestValue: Boolean = false,
)
data class ApiErrorResponse(val error: String? = null)

fun ProductOfferDto.toProductOffer() = ProductOffer(
    id, store, productName, price, originalPrice, shipping, delivery, rating, sourceUrl, summary, tags, isBestValue,
)
