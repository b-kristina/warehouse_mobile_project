package ru.vsu.warehouse.features.supplies.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.SupplyResponse
import ru.vsu.warehouse.features.supplies.data.SupplyRepository
import ru.vsu.warehouse.features.supplies.data.model.SupplyCreateRequest
import ru.vsu.warehouse.features.supplies.domain.CreateSupplyUseCase
import ru.vsu.warehouse.features.supplies.domain.DeleteSupplyUseCase

class SuppliesViewModel : ViewModel() {

    private val repository = SupplyRepository()

    private val _supplies = MutableStateFlow<List<SupplyResponse>>(emptyList())
    val supplies: StateFlow<List<SupplyResponse>> = _supplies

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentCategoryFilter: List<Int>? = null
    private var currentProviderFilter: List<Int>? = null

    fun loadSupplies(
        categoryIds: List<Int>? = null,
        providerIds: List<Int>? = null
    ) {
        currentCategoryFilter = categoryIds
        currentProviderFilter = providerIds

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val page: PageResponse<SupplyResponse> = repository.getSupplies(
                    categoryIds = categoryIds,
                    providerIds = providerIds,
                    size = 500 // чтобы получить все поставки
                )
                _supplies.value = page.content
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки поставок"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSupply(request: SupplyCreateRequest) {
        viewModelScope.launch {
            val result = CreateSupplyUseCase(repository)(request)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка создания"
            }
        }
    }

    fun deleteSupply(id: Int) {
        viewModelScope.launch {
            val result = DeleteSupplyUseCase(repository)(id)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка удаления"
            }
        }
    }

    private fun reload() {
        loadSupplies(currentCategoryFilter, currentProviderFilter)
    }
}