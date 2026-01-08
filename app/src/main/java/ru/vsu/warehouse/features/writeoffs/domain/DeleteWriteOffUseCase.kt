package ru.vsu.warehouse.features.writeoffs.domain

import ru.vsu.warehouse.features.writeoffs.data.WriteOffRepository

class DeleteWriteOffUseCase(
    private val repository: WriteOffRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.deleteWriteOff(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}