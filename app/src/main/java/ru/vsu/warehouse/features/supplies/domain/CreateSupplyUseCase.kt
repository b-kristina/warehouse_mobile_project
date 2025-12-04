package ru.vsu.warehouse.features.supplies.domain

import ru.vsu.warehouse.data.model.SupplyResponse
import ru.vsu.warehouse.features.supplies.data.SupplyRepository
import ru.vsu.warehouse.features.supplies.data.model.SupplyCreateRequest

class CreateSupplyUseCase(
    private val repository: SupplyRepository
) {
    suspend operator fun invoke(request: SupplyCreateRequest): Result<SupplyResponse> {
        return try {
            val supply = repository.createSupply(request)
            Result.success(supply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}