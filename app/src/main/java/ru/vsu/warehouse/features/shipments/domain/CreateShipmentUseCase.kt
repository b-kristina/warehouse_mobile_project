package ru.vsu.warehouse.features.shipments.domain

import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.features.shipments.data.ShipmentRepository
import ru.vsu.warehouse.features.shipments.data.model.ShipmentCreateRequest

class CreateShipmentUseCase(
    private val repository: ShipmentRepository
) {
    suspend operator fun invoke(request: ShipmentCreateRequest): Result<ShipmentResponse> {
        return try {
            val shipment = repository.createShipment(request)
            Result.success(shipment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}