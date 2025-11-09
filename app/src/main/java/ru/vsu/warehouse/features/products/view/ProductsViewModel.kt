package ru.vsu.warehouse.features.products.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.Product
import ru.vsu.warehouse.features.products.data.ProductRepository
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest
import ru.vsu.warehouse.features.products.domain.UpdateProductUseCase

class ProductsViewModel : ViewModel() {

    private val repository = ProductRepository()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val page = repository.getAllProducts(size = 20)
                _products.value = page.content
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProduct(id: Int, title: String, categoryIds: List<Int>) {
        viewModelScope.launch {
            val request = ProductUpdateRequest(title, categoryIds)
            val result = UpdateProductUseCase(repository)(id, request)
            if (result.isSuccess) {
                loadProducts()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}