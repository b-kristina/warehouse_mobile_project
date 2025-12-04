package ru.vsu.warehouse.features.supplies.view

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
import kotlinx.coroutines.launch
import ru.vsu.warehouse.R
import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.*
import ru.vsu.warehouse.databinding.ActivitySuppliesBinding
import ru.vsu.warehouse.features.supplies.data.model.NewProviderRequest
import ru.vsu.warehouse.features.supplies.data.model.SupplyCreateRequest
import java.time.LocalDate

class SuppliesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuppliesBinding
    private val viewModel: SuppliesViewModel by viewModels()

    // Списки для формы
    private var products: List<ProductSimpleResponse> = emptyList()
    private var categories: List<CategoryResponse> = emptyList()
    private var providers: List<ProviderSimpleResponse> = emptyList()

    private var selectedCategoryFilter: MutableList<Int> = mutableListOf()
    private var selectedProviderFilter: MutableList<Int> = mutableListOf()
    private var showCategoryFilterDropdown = false
    private var showProviderFilterDropdown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuppliesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewSupplies.layoutManager = LinearLayoutManager(this)

        val adapter = SupplyAdapter { supplyId ->
            showDeleteConfirmation(supplyId)
        }
        binding.recyclerViewSupplies.adapter = adapter

        // Подписка на поставки
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.supplies.collect { supplies ->
                    adapter.submitList(supplies)
                }
            }
        }

        // Подписка на ошибки
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@SuppliesActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Сбрасываем фильтры
        selectedCategoryFilter.clear()
        selectedProviderFilter.clear()

        loadListsForForm()
        applyFilters() // загружает все поставки (без фильтров)
    }

    private fun loadListsForForm() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                products = api.getSimpleProducts()
                categories = api.getAllCategories()
                providers = api.getSimpleProviders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openNewSupplyDialog(view: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_supply, null)
        val spinnerProduct = dialogView.findViewById<Spinner>(R.id.spinnerProduct)
        val editTextNewProduct = dialogView.findViewById<EditText>(R.id.editTextNewProduct)
        val listViewCategories = dialogView.findViewById<ListView>(R.id.listViewCategories)
        val spinnerProvider = dialogView.findViewById<Spinner>(R.id.spinnerProvider)
        val editTextNewProviderName = dialogView.findViewById<EditText>(R.id.editTextNewProviderName)
        val editTextNewProviderInn = dialogView.findViewById<EditText>(R.id.editTextNewProviderInn)
        val editTextNewProviderCompany = dialogView.findViewById<EditText>(R.id.editTextNewProviderCompany)
        val editTextNewProviderAddress = dialogView.findViewById<EditText>(R.id.editTextNewProviderAddress)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)

        // Настройка даты (сегодня по умолчанию)
        val today = LocalDate.now().toString()
        editTextDate.setText(today)

        // Настройка списка товаров
        val productTitles = listOf("Выберите товар", "➕ Добавить новый") +
                products.map { it.productTitle }
        val productAdapter = ArrayAdapter(this, R.layout.spinner_item, productTitles)
        productAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerProduct.adapter = productAdapter

        spinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                editTextNewProduct.visibility = if (position == 1) View.VISIBLE else View.GONE
                if (position > 1) {
                    val selectedProduct = products[position - 2]
                    // Восстанавливаем категории
                    val selectedIds = selectedProduct.categoryIds
                    for (i in categories.indices) {
                        listViewCategories.setItemChecked(i, selectedIds.contains(categories[i].categoryId))
                    }
                } else {
                    for (i in categories.indices) {
                        listViewCategories.setItemChecked(i, false)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Категории (множественный выбор)
        val categoryTitles = categories.map { it.categoryTitle }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, categoryTitles)
        listViewCategories.adapter = categoryAdapter
        listViewCategories.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Поставщики
        val providerTitles = listOf("Выберите поставщика", "➕ Добавить нового") +
                providers.map { it.providerName }
        val providerAdapter = ArrayAdapter(this, R.layout.spinner_item, providerTitles)
        providerAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerProvider.adapter = providerAdapter

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val isNew = position == 1
                editTextNewProviderName.visibility = if (isNew) View.VISIBLE else View.GONE
                editTextNewProviderInn.visibility = if (isNew) View.VISIBLE else View.GONE
                editTextNewProviderCompany.visibility = if (isNew) View.VISIBLE else View.GONE
                editTextNewProviderAddress.visibility = if (isNew) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Новая поставка")
            .setView(dialogView)
            .setPositiveButton("Создать") { _, _ ->
                // Валидация товара
                val productId: Int?
                val newProductTitle: String?
                when (spinnerProduct.selectedItemPosition) {
                    0 -> {
                        Toast.makeText(this, "Выберите товар", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    1 -> {
                        newProductTitle = editTextNewProduct.text.toString().trim()
                        if (newProductTitle.isEmpty()) {
                            Toast.makeText(this, "Введите название товара", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        productId = null
                    }
                    else -> {
                        productId = products[spinnerProduct.selectedItemPosition - 2].productId
                        newProductTitle = null
                    }
                }

                // Валидация категорий
                val categoryIds = mutableListOf<Int>()
                val checked = listViewCategories.checkedItemPositions
                for (i in categories.indices) {
                    if (checked[i]) {
                        categoryIds.add(categories[i].categoryId)
                    }
                }

                // Валидация поставщика
                val providerId: Int?
                val newProvider: NewProviderRequest?
                when (spinnerProvider.selectedItemPosition) {
                    0 -> {
                        Toast.makeText(this, "Выберите поставщика", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    1 -> {
                        val name = editTextNewProviderName.text.toString().trim()
                        val inn = editTextNewProviderInn.text.toString().trim()
                        val company = editTextNewProviderCompany.text.toString().trim()
                        val address = editTextNewProviderAddress.text.toString().trim()
                        if (name.isEmpty() || inn.isEmpty() || company.isEmpty() || address.isEmpty()) {
                            Toast.makeText(this, "Заполните все поля нового поставщика", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        if (inn.length != 12 || !inn.all { it.isDigit() }) {
                            Toast.makeText(this, "ИНН должен содержать 12 цифр", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        providerId = null
                        newProvider = NewProviderRequest(name, inn, company, address)
                    }
                    else -> {
                        providerId = providers[spinnerProvider.selectedItemPosition - 2].providerId
                        newProvider = null
                    }
                }

                // Количество
                val quantityStr = editTextQuantity.text.toString().trim()
                if (quantityStr.isEmpty()) {
                    Toast.makeText(this, "Введите количество", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val quantity = quantityStr.toIntOrNull()
                if (quantity == null || quantity < 1) {
                    Toast.makeText(this, "Количество должно быть >= 1", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Дата
                val dateStr = editTextDate.text.toString().trim()
                val date = try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    Toast.makeText(this, "Неверный формат даты (гггг-мм-дд)", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Создаём запрос
                val request = SupplyCreateRequest(
                    productId = productId,
                    newProductTitle = newProductTitle,
                    categoryIds = if (categoryIds.isEmpty()) null else categoryIds,
                    providerId = providerId,
                    newProvider = newProvider,
                    supplyQuantity = quantity,
                    supplyDate = date
                )

                viewModel.createSupply(request)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    fun openFilterDialog(view: View) {
        showFilterDialog()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_supplies, null)
        val categoryListView = dialogView.findViewById<ListView>(R.id.listViewCategoryFilter)
        val providerListView = dialogView.findViewById<ListView>(R.id.listViewProviderFilter)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClearFilters)

        // Категории
        val categoryTitles = categories.map { it.categoryTitle }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, categoryTitles)
        categoryListView.adapter = categoryAdapter
        categoryListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Восстанавливаем выбор
        for (i in categories.indices) {
            categoryListView.setItemChecked(i, selectedCategoryFilter.contains(categories[i].categoryId))
        }

        // Поставщики
        val providerTitles = providers.map { it.providerName }
        val providerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, providerTitles)
        providerListView.adapter = providerAdapter
        providerListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        for (i in providers.indices) {
            providerListView.setItemChecked(i, selectedProviderFilter.contains(providers[i].providerId))
        }

        // Кнопка "Сбросить"
        btnClear.setOnClickListener {
            selectedCategoryFilter.clear()
            selectedProviderFilter.clear()
            for (i in categories.indices) categoryListView.setItemChecked(i, false)
            for (i in providers.indices) providerListView.setItemChecked(i, false)
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Фильтры поставок")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ ->
                // Собираем выбранные категории
                selectedCategoryFilter.clear()
                val catChecked = categoryListView.checkedItemPositions
                for (i in categories.indices) {
                    if (catChecked[i]) {
                        selectedCategoryFilter.add(categories[i].categoryId)
                    }
                }

                // Собираем выбранных поставщиков
                selectedProviderFilter.clear()
                val provChecked = providerListView.checkedItemPositions
                for (i in providers.indices) {
                    if (provChecked[i]) {
                        selectedProviderFilter.add(providers[i].providerId)
                    }
                }

                // Применяем фильтры
                applyFilters()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun applyFilters() {
        val catFilter = if (selectedCategoryFilter.isEmpty()) null else selectedCategoryFilter.toList()
        val provFilter = if (selectedProviderFilter.isEmpty()) null else selectedProviderFilter.toList()
        viewModel.loadSupplies(catFilter, provFilter)
    }

    private fun showDeleteConfirmation(supplyId: Int) {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены? Количество товара будет уменьшено на складе.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteSupply(supplyId)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}