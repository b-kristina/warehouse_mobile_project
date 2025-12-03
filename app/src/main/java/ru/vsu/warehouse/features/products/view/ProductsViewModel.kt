package ru.vsu.warehouse.features.products.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.features.products.data.ProductRepository
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest
import ru.vsu.warehouse.features.products.domain.UpdateProductUseCase

class ProductsViewModel : ViewModel() {

    private val repository = ProductRepository()

    private var currentFilter: String? = null
    private var currentCategoryIds: List<Int>? = null

    private val _products = MutableStateFlow<PagingData<ProductResponse>>(PagingData.empty())
    val products: Flow<PagingData<ProductResponse>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadProducts()
    }

    fun loadProducts(
        filter: String? = null,
        categoryIds: List<Int>? = null
    ) {
        currentFilter = filter
        currentCategoryIds = categoryIds

        val flow = repository.getProductsPaging(filter, categoryIds)
            .cachedIn(viewModelScope) // кэширует данные при повороте

        _products.value = PagingData.empty() // сброс
        viewModelScope.launch {
            flow.collect { pagingData ->
                _products.value = pagingData
            }
        }
    }

    fun updateProduct(id: Int, title: String, categoryIds: List<Int>) {
        viewModelScope.launch {
            val request = ProductUpdateRequest(title, categoryIds)
            val result = UpdateProductUseCase(repository)(id, request)
            if (result.isSuccess) {
                loadProducts(currentFilter, currentCategoryIds) // перезагрузить
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}