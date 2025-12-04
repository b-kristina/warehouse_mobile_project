package ru.vsu.warehouse.features.providers.domain

import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.features.providers.data.ProviderRepository
import ru.vsu.warehouse.features.providers.data.model.ProviderUpdateRequest

class UpdateProviderUseCase(
    private val repository: ProviderRepository
) {
    suspend operator fun invoke(id: Int, request: ProviderUpdateRequest): Result<ProviderResponse> {
        return try {
            val updated = repository.updateProvider(id, request)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}