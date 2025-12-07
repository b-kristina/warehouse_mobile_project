package ru.vsu.warehouse.features.shipments.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.ProviderSimpleResponse
import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.features.shipments.data.ShipmentRepository
import ru.vsu.warehouse.features.shipments.data.model.ShipmentCreateRequest
import ru.vsu.warehouse.features.shipments.domain.CreateShipmentUseCase
import ru.vsu.warehouse.features.shipments.domain.DeleteShipmentUseCase

class ShipmentViewModel : ViewModel() {

    private val repository = ShipmentRepository()

    private val _shipments = MutableStateFlow<List<ShipmentResponse>>(emptyList())
    val shipments: StateFlow<List<ShipmentResponse>> = _shipments

    private val _products = MutableStateFlow<List<ProductSimpleResponse>>(emptyList())
    val products: StateFlow<List<ProductSimpleResponse>> = _products

    private val _providers = MutableStateFlow<List<ProviderSimpleResponse>>(emptyList())
    val providers: StateFlow<List<ProviderSimpleResponse>> = _providers

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentProviderFilter: List<Int>? = null

    init {
        loadLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            try {
                val products = repository.getSimpleProducts()
                val providers = repository.getSimpleProviders()
                _products.value = products
                _providers.value = providers
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки списков"
            }
        }
    }

    fun loadShipments(providerIds: List<Int>? = null) {
        currentProviderFilter = providerIds
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val page: PageResponse<ShipmentResponse> = repository.getShipments(
                    providerIds = providerIds,
                    size = 50
                )
                _shipments.value = page.content
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки отгрузок"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reload() {
        loadShipments(currentProviderFilter)
    }

    fun createShipment(request: ShipmentCreateRequest) {
        viewModelScope.launch {
            val result = CreateShipmentUseCase(repository)(request)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка создания отгрузки"
            }
        }
    }

    fun deleteShipment(id: Int) {
        viewModelScope.launch {
            val result = DeleteShipmentUseCase(repository)(id)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка удаления отгрузки"
            }
        }
    }
}