package ru.vsu.warehouse.features.providers.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.features.providers.data.ProviderRepository
import ru.vsu.warehouse.features.providers.data.model.ProviderUpdateRequest
import ru.vsu.warehouse.features.providers.domain.UpdateProviderUseCase

class ProvidersViewModel : ViewModel() {

    private val repository = ProviderRepository()

    private val _providers = MutableStateFlow<List<ProviderResponse>>(emptyList())
    val providers: StateFlow<List<ProviderResponse>> = _providers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentFilter: String? = null

    fun loadProviders(filterType: String? = null) {
        currentFilter = filterType
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val page = repository.getProviders(
                    filter = filterType, // передаём filterType
                    size = 50
                )
                _providers.value = page.content
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reload() {
        loadProviders(currentFilter)
    }

    fun updateProvider(
        id: Int,
        providerName: String,
        inn: String,
        companyName: String,
        companyAddress: String
    ) {
        viewModelScope.launch {
            val request = ProviderUpdateRequest(
                providerName = providerName,
                inn = inn,
                companyName = companyName,
                companyAddress = companyAddress
            )
            val result = UpdateProviderUseCase(repository)(id, request)
            if (result.isSuccess) {
                loadProviders(currentFilter)
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка обновления"
            }
        }
    }
}