package ru.vsu.warehouse.features.categories.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.CategoryResponse

class CategoryRepository {
    private val api = RetrofitClient.api

    suspend fun getAllCategories(): List<CategoryResponse> {
        return api.getAllCategories()
    }
}