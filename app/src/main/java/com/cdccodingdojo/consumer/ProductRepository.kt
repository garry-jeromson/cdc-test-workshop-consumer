package com.cdccodingdojo.consumer

import Product

class ProductNotFoundException(id: Int) : Exception("Product with ID $id not found.")

interface ProductRepositoryInterface {
    suspend fun getProductInfo(productId: Int): Product?;
}

class ProductRepository(private val productApi: ProductApiClient) : ProductRepositoryInterface {

    override suspend fun getProductInfo(productId: Int): Product? {
        return productApi.getProductInfo(productId)
    }
}

