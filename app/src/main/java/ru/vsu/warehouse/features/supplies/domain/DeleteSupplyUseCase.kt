package ru.vsu.warehouse.features.supplies.domain

import ru.vsu.warehouse.features.supplies.data.SupplyRepository

class DeleteSupplyUseCase(
    private val repository: SupplyRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.deleteSupply(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}