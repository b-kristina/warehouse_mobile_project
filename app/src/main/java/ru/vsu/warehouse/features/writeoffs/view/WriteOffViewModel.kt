package ru.vsu.warehouse.features.writeoffs.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.WriteOffResponse
import ru.vsu.warehouse.features.writeoffs.data.WriteOffRepository
import ru.vsu.warehouse.features.writeoffs.data.model.WriteOffCreateRequest
import ru.vsu.warehouse.features.writeoffs.domain.CreateWriteOffUseCase
import ru.vsu.warehouse.features.writeoffs.domain.DeleteWriteOffUseCase

class WriteOffViewModel : ViewModel() {

    private val repository = WriteOffRepository()

    private val _writeOffs = MutableStateFlow<List<WriteOffResponse>>(emptyList())
    val writeOffs: StateFlow<List<WriteOffResponse>> = _writeOffs

    private val _products = MutableStateFlow<List<ProductSimpleResponse>>(emptyList())
    val products: StateFlow<List<ProductSimpleResponse>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentReasons: List<String>? = null
    private var currentIncludeNonStandard: Boolean = false

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = repository.getSimpleProducts()
                _products.value = products
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки товаров"
            }
        }
    }

    fun loadWriteOffs(reasons: List<String>? = null, includeNonStandard: Boolean = false) {
        currentReasons = reasons
        currentIncludeNonStandard = includeNonStandard

        viewModelScope.launch {
            try {
                val page: PageResponse<WriteOffResponse> = repository.getWriteOffs(
                    reasons = reasons,
                    includeNonStandard = includeNonStandard,
                    size = 500
                )
                _writeOffs.value = page.content
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Ошибка загрузки списаний"
            }
        }
    }

    fun createWriteOff(request: WriteOffCreateRequest) {
        viewModelScope.launch {
            val result = CreateWriteOffUseCase(repository)(request)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка создания списания"
            }
        }
    }

    fun deleteWriteOff(id: Int) {
        viewModelScope.launch {
            val result = DeleteWriteOffUseCase(repository)(id)
            if (result.isSuccess) {
                reload()
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Ошибка удаления списания"
            }
        }
    }

    private fun reload() {
        loadWriteOffs(currentReasons, currentIncludeNonStandard)
    }
}