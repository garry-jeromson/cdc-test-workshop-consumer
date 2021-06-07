package com.cdccodingdojo.consumer

import Product
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.lang.Exception

private val configuredHttpClient = HttpClient(Android) {

    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = false
            ignoreUnknownKeys = true
        })
    }

    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }


}

class ProductApiException(message: String) : Exception(message)

class ProductApiClient(private val baseUrl: Url) {

    suspend fun getProductInfo(
        productId: Int
    ): Product? {
        try {
            return configuredHttpClient.get(
                host = baseUrl.host,
                port = baseUrl.port,
                path = "/products/$productId"
            )
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                throw ProductNotFoundException(productId)
            } else {
                throw ProductApiException("Unhandled product API error: ${e.response.status}, ${e.response.content}")
            }
        }
    }
}
