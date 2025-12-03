package ru.vsu.warehouse.features.products.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.vsu.warehouse.R
import ru.vsu.warehouse.data.model.CategoryResponse
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.databinding.ActivityProductsBinding

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private val viewModel: ProductsViewModel by viewModels()

    private var allCategories: List<CategoryResponse> = emptyList()
    private var selectedFilter: String = "all"
    private var selectedCategoryIds: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)

        // Подписка на список товаров
        val adapter = ProductAdapter { product ->
            openEditDialog(product)
        }
        binding.recyclerViewProducts.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        // Подписка на ошибки
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@ProductsActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Сразу загружаем ВСЕ товары (без фильтров)
        viewModel.loadProducts()

        // Загружаем категории для фильтра — в фоне
        loadCategoriesForFilter()
    }

    private fun loadCategoriesForFilter() {
        lifecycleScope.launch {
            try {
                val api = ru.vsu.warehouse.data.api.RetrofitClient.api
                allCategories = api.getAllCategories()
            } catch (e: Exception) {
                // Ошибка не ломает основной функционал
            }
        }
    }

    fun openFilterDialog(view: View) {
        showFilterDialog()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupFilter)
        val categoryListView = dialogView.findViewById<ListView>(R.id.listViewCategories)
        val checkboxAll = dialogView.findViewById<CheckBox>(R.id.checkboxAll)

        // 🔥 Восстанавливаем текущий фильтр по наличию
        when (selectedFilter) {
            "in_stock" -> radioGroup.check(R.id.radioInStock)
            "out_of_stock" -> radioGroup.check(R.id.radioOutOfStock)
            else -> radioGroup.check(R.id.radioAll)
        }

        if (allCategories.isEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("Загрузка категорий..."))
            categoryListView.adapter = adapter
            categoryListView.isEnabled = false
            checkboxAll.visibility = View.GONE
        } else {
            val categoryTitles = allCategories.map { it.categoryTitle }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, categoryTitles)
            categoryListView.adapter = adapter
            categoryListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

            // 🔥 Восстанавливаем выбранные категории
            for (i in allCategories.indices) {
                val isSelected = selectedCategoryIds.contains(allCategories[i].categoryId)
                categoryListView.setItemChecked(i, isSelected)
            }

            // Обновляем состояние "Выбрать все"
            val allChecked = selectedCategoryIds.size == allCategories.size
            checkboxAll.isChecked = allChecked

            checkboxAll.setOnCheckedChangeListener { _, isChecked ->
                for (i in 0 until adapter.count) {
                    categoryListView.setItemChecked(i, isChecked)
                }
            }
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Фильтры")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ ->
                if (allCategories.isEmpty()) return@setPositiveButton

                // Сохраняем новый фильтр по наличию
                selectedFilter = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioInStock -> "in_stock"
                    R.id.radioOutOfStock -> "out_of_stock"
                    else -> "all"
                }

                // Собираем выбранные категории
                selectedCategoryIds.clear()
                val checked = categoryListView.checkedItemPositions
                for (i in allCategories.indices) {
                    if (checked[i]) {
                        selectedCategoryIds.add(allCategories[i].categoryId)
                    }
                }

                // Передаём фильтры в ViewModel
                val categoryList = if (selectedCategoryIds.isEmpty()) null else selectedCategoryIds.toList()
                val filterValue = if (selectedFilter == "all") null else selectedFilter

                viewModel.loadProducts(filter = filterValue, categoryIds = categoryList)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun openEditDialog(product: ProductResponse) {
        // Убедись, что категории загружены
        if (allCategories.isEmpty()) {
            Toast.makeText(this, "Категории ещё загружаются...", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val listViewCategories = dialogView.findViewById<ListView>(R.id.listViewCategories)

        editTextTitle.setText(product.productTitle)

        // Подготовка адаптера категорий
        val categoryTitles = allCategories.map { it.categoryTitle }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, categoryTitles)
        listViewCategories.adapter = adapter
        listViewCategories.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Восстановление выбранных категорий
        val currentCategoryIds = product.categoryTitles.mapNotNull { title ->
            allCategories.find { it.categoryTitle == title }?.categoryId
        }
        for (i in allCategories.indices) {
            if (currentCategoryIds.contains(allCategories[i].categoryId)) {
                listViewCategories.setItemChecked(i, true)
            }
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Редактировать товар")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = editTextTitle.text.toString().trim()
                if (title.isEmpty()) {
                    Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedIds = mutableListOf<Int>()
                val checked = listViewCategories.checkedItemPositions
                for (i in allCategories.indices) {
                    if (checked[i]) {
                        selectedIds.add(allCategories[i].categoryId)
                    }
                }

                // Обновление через ViewModel
                viewModel.updateProduct(product.productId, title, selectedIds)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}