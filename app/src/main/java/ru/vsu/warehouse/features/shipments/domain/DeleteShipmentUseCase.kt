package ru.vsu.warehouse.features.shipments.domain

import ru.vsu.warehouse.features.shipments.data.ShipmentRepository

class DeleteShipmentUseCase(
    private val repository: ShipmentRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.deleteShipment(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}