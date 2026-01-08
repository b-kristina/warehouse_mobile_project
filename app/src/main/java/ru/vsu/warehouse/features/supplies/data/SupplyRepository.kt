package ru.vsu.warehouse.features.supplies.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.SupplyResponse
import ru.vsu.warehouse.features.supplies.data.model.SupplyCreateRequest

class SupplyRepository {
    private val api = RetrofitClient.api

    suspend fun getSupplies(
        categoryIds: List<Int>? = null,
        providerIds: List<Int>? = null,
        page: Int = 0,
        size: Int = 15
    ): PageResponse<SupplyResponse> {
        return api.getSupplies(categoryIds, providerIds, page, size)
    }

    suspend fun createSupply(request: SupplyCreateRequest): SupplyResponse {
        return api.createSupply(request)
    }

    suspend fun deleteSupply(id: Int) {
        val response = api.deleteSupply(id)
        if (!response.isSuccessful) {
            throw Exception("Скорее всего товар уже уехал, HTTP ${response.code()}: ${response.message()}")
        }
    }
}