package ru.vsu.warehouse.features.writeoffs.domain

import ru.vsu.warehouse.data.model.WriteOffResponse
import ru.vsu.warehouse.features.writeoffs.data.WriteOffRepository
import ru.vsu.warehouse.features.writeoffs.data.model.WriteOffCreateRequest

class CreateWriteOffUseCase(
    private val repository: WriteOffRepository
) {
    suspend operator fun invoke(request: WriteOffCreateRequest): Result<WriteOffResponse> {
        return try {
            val writeOff = repository.createWriteOff(request)
            Result.success(writeOff)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}