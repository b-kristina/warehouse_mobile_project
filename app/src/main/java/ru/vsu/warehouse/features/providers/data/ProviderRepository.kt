package ru.vsu.warehouse.features.providers.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.features.providers.data.model.ProviderUpdateRequest

class ProviderRepository {
    private val api = RetrofitClient.api

    suspend fun getProviders(
        filter: String? = null,
        page: Int = 0,
        size: Int = 10
    ): PageResponse<ProviderResponse> {
        return api.getProviders(filter, page, size)
    }

    suspend fun getProviderById(id: Int): ProviderResponse {
        return api.getProviderById(id)
    }

    suspend fun updateProvider(id: Int, request: ProviderUpdateRequest): ProviderResponse {
        return api.updateProvider(id, request)
    }
}