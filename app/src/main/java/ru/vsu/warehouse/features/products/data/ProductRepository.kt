package ru.vsu.warehouse.features.products.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.Product
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest

class ProductRepository {
    private val api = RetrofitClient.api

    suspend fun getAllProducts(
        filter: String? = null,
        categoryId: List<Int>? = null,
        page: Int = 0,
        size: Int = 10
    ): PageResponse<Product> {
        return api.getAllProducts(filter, categoryId, page, size)
    }

    suspend fun getProductById(id: Int): Product {
        return api.getProductById(id)
    }

    suspend fun updateProduct(id: Int, request: ProductUpdateRequest): Product {
        return api.updateProduct(id, request)
    }
}