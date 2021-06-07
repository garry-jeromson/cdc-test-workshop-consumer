package com.cdccodingdojo.consumer

import Product
import au.com.dius.pact.consumer.dsl.PactDslResponse
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.DefaultResponseValues
import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import io.ktor.http.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.*


const val consumerName = "subitogo"
const val providerName = "product_service"

/**
 * Example consumer CDC test, which generates a JSON pact file later used to verify the behaviour of
 * the provider.
 */
class TestProductDataQueries {
    @Rule
    @JvmField
    var provider: PactProviderRule = PactProviderRule(providerName, this)

    @DefaultResponseValues
    fun defaultResponseValues(response: PactDslResponse) {
        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        response.headers(headers)
    }

    @Pact(consumer = consumerName)
    fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given("a product with ID 123 exists")
            .uponReceiving("a request for the product info with ID 123")
            .path("/products/123")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body("{\"id\": 123, \"name\": \"Twix\", \"price\": 1.50}")
            .given("no product with ID 456 exists")
            .uponReceiving("a request for the product info with ID 456")
            .path("/products/456")
            .method("GET")
            .willRespondWith()
            .status(404)
            .body("{\"message\": \"Product not found\"}")
            .toPact()
    }

    @Test
    @PactVerification
    @Throws(IOException::class)
    fun testProductDataRetrieval(): Unit = runBlocking {
        val apiClient = ProductApiClient(Url(provider.url))
        val productRepo = ProductRepository(apiClient)
        val expectedResponse = Product(
            id = 123,
            name = "Twix",
            price = 1.50
        )
        val response = productRepo.getProductInfo(123)
        assertEquals(expectedResponse, response)

        try {
            productRepo.getProductInfo(456)
        } catch (e: Exception) {
            assertTrue(e is ProductNotFoundException)
        }

    }
}