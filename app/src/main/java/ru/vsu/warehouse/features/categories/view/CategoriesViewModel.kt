package ru.vsu.warehouse.features.categories.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.CategoryResponse
import ru.vsu.warehouse.features.categories.data.CategoryRepository

class CategoriesViewModel : ViewModel() {

    private val repository = CategoryRepository()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getAllCategories()
                _categories.value = list
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }
}