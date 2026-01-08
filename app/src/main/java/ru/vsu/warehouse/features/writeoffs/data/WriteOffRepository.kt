package ru.vsu.warehouse.features.writeoffs.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.WriteOffResponse
import ru.vsu.warehouse.features.writeoffs.data.model.WriteOffCreateRequest

class WriteOffRepository {
    private val api = RetrofitClient.api

    suspend fun getWriteOffs(
        reasons: List<String>? = null,
        includeNonStandard: Boolean = false,
        page: Int = 0,
        size: Int = 15
    ): PageResponse<WriteOffResponse> {
        return api.getWriteOffs(reasons, includeNonStandard, page, size)
    }

    suspend fun createWriteOff(request: WriteOffCreateRequest): WriteOffResponse {
        return api.createWriteOff(request)
    }

    suspend fun deleteWriteOff(id: Int) {
        val response = api.deleteWriteOff(id)
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }

    suspend fun getSimpleProducts(): List<ProductSimpleResponse> {
        return api.getSimpleProductsForWriteOffs()
    }
}