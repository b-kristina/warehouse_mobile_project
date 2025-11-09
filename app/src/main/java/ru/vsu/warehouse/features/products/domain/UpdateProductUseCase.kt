package ru.vsu.warehouse.features.products.domain

import ru.vsu.warehouse.data.model.Product
import ru.vsu.warehouse.features.products.data.ProductRepository
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest

class UpdateProductUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(id: Int, request: ProductUpdateRequest): Result<Product> {
        return try {
            val updated = repository.updateProduct(id, request)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}