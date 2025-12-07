package ru.vsu.warehouse.features.shipments.data

import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.ProviderSimpleResponse
import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.features.shipments.data.model.ShipmentCreateRequest

class ShipmentRepository {
    private val api = RetrofitClient.api

    suspend fun getShipments(
        providerIds: List<Int>? = null,
        page: Int = 0,
        size: Int = 15
    ): PageResponse<ShipmentResponse> {
        return api.getShipments(providerIds, page, size)
    }

    suspend fun createShipment(request: ShipmentCreateRequest): ShipmentResponse {
        return api.createShipment(request)
    }

    suspend fun deleteShipment(id: Int) {
        val response = api.deleteShipment(id)
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }

    suspend fun getSimpleProducts(): List<ProductSimpleResponse> {
        return api.getSimpleProductsForShipments()
    }

    suspend fun getSimpleProviders(): List<ProviderSimpleResponse> {
        return api.getSimpleProvidersForShipments()
    }
}