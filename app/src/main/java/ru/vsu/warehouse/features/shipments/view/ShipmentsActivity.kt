package ru.vsu.warehouse.features.shipments.view

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ru.vsu.warehouse.R
import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.ProviderSimpleResponse
import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.databinding.ActivityShipmentsBinding
import ru.vsu.warehouse.features.supplies.data.model.NewProviderRequest
import ru.vsu.warehouse.features.shipments.data.model.ShipmentCreateRequest
import ru.vsu.warehouse.utils.SwipeToDeleteCallback
import java.time.LocalDate

class ShipmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShipmentsBinding
    private val viewModel: ShipmentViewModel by viewModels()

    private var products: List<ProductSimpleResponse> = emptyList()
    private var providers: List<ProviderSimpleResponse> = emptyList()

    private var selectedProviderFilter: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShipmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewShipments.layoutManager = LinearLayoutManager(this)

        val adapter = ShipmentAdapter { shipmentId ->
            showDeleteConfirmation(shipmentId, null)
        }
        binding.recyclerViewShipments.adapter = adapter

        val swipeToDelete = SwipeToDeleteCallback(this) { position ->
            val shipment = adapter.getShipmentAt(position)
            showDeleteConfirmation(shipment.shipmentId, position)
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerViewShipments)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shipments.collect { shipments ->
                    adapter.submitList(shipments)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@ShipmentsActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        selectedProviderFilter.clear()
        loadListsForForm()
        applyFilters()
    }

    private fun loadListsForForm() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                products = api.getSimpleProductsForShipments()
                providers = api.getSimpleProvidersForShipments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openNewShipmentDialog(view: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_shipment, null)
        val spinnerProduct = dialogView.findViewById<Spinner>(R.id.spinnerProduct)
        val spinnerProvider = dialogView.findViewById<Spinner>(R.id.spinnerProvider)
        val wrapperNewProviderName = dialogView.findViewById<TextInputLayout>(R.id.wrapperNewProviderName)
        val wrapperNewProviderInn = dialogView.findViewById<TextInputLayout>(R.id.wrapperNewProviderInn)
        val wrapperNewProviderCompany = dialogView.findViewById<TextInputLayout>(R.id.wrapperNewProviderCompany)
        val wrapperNewProviderAddress = dialogView.findViewById<TextInputLayout>(R.id.wrapperNewProviderAddress)
        val editTextNewProviderName = dialogView.findViewById<EditText>(R.id.editTextNewProviderName)
        val editTextNewProviderInn = dialogView.findViewById<EditText>(R.id.editTextNewProviderInn)
        val editTextNewProviderCompany = dialogView.findViewById<EditText>(R.id.editTextNewProviderCompany)
        val editTextNewProviderAddress = dialogView.findViewById<EditText>(R.id.editTextNewProviderAddress)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)

        val today = LocalDate.now().toString()
        editTextDate.setText(today)

        // ✅ ДОБАВЛЕН ПУНКТ "Выберите товар"
        val productTitles = listOf("Выберите товар") +
                products.map { "${it.productTitle} (остаток: ${it.currentCount})" }
        val productAdapter = ArrayAdapter(this, R.layout.spinner_item, productTitles)
        productAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerProduct.adapter = productAdapter

        // Поставщики
        val providerTitles = listOf("Выберите поставщика", "➕ Добавить нового") + providers.map { it.providerName }
        val providerAdapter = ArrayAdapter(this, R.layout.spinner_item, providerTitles)
        providerAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerProvider.adapter = providerAdapter

        spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val isNew = position == 1
                wrapperNewProviderName.visibility = if (isNew) View.VISIBLE else View.GONE
                wrapperNewProviderInn.visibility = if (isNew) View.VISIBLE else View.GONE
                wrapperNewProviderCompany.visibility = if (isNew) View.VISIBLE else View.GONE
                wrapperNewProviderAddress.visibility = if (isNew) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            // ✅ ПРАВИЛЬНАЯ ВАЛИДАЦИЯ С ИНДЕКСОМ -1
            val productId: Int?
            when (spinnerProduct.selectedItemPosition) {
                0 -> {
                    Toast.makeText(this, "Выберите товар", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    productId = products[spinnerProduct.selectedItemPosition - 1].productId
                }
            }

            // Проверка остатка (с тем же индексом)
            val selectedProduct = products[spinnerProduct.selectedItemPosition - 1]
            val quantityStr = editTextQuantity.text.toString().trim()
            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Введите количество", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val quantity = quantityStr.toIntOrNull()
            if (quantity == null || quantity < 1) {
                Toast.makeText(this, "Количество должно быть >= 1", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (quantity > selectedProduct.currentCount) {
                Toast.makeText(this, "Недостаточно товара. Доступно: ${selectedProduct.currentCount}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val providerId: Int?
            val newProvider: NewProviderRequest?
            when (spinnerProvider.selectedItemPosition) {
                0 -> {
                    Toast.makeText(this, "Выберите поставщика", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                1 -> {
                    val name = editTextNewProviderName.text.toString().trim()
                    val inn = editTextNewProviderInn.text.toString().trim()
                    val company = editTextNewProviderCompany.text.toString().trim()
                    val address = editTextNewProviderAddress.text.toString().trim()
                    if (name.isEmpty() || inn.isEmpty() || company.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Заполните все поля нового поставщика", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (inn.length != 12 || !inn.all { it.isDigit() }) {
                        Toast.makeText(this, "ИНН должен содержать 12 цифр", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    providerId = null
                    newProvider = NewProviderRequest(name, inn, company, address)
                }
                else -> {
                    providerId = providers[spinnerProvider.selectedItemPosition - 2].providerId
                    newProvider = null
                }
            }

            val dateStr = editTextDate.text.toString().trim()
            val date = try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                Toast.makeText(this, "Неверный формат даты (гггг-мм-дд)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ShipmentCreateRequest(
                productId = productId,
                providerId = providerId,
                newProvider = newProvider,
                shipmentQuantity = quantity,
                shipmentDate = date
            )

            viewModel.createShipment(request)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun openFilterDialog(view: View) {
        showFilterDialog()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_shipments, null)
        val providerListView = dialogView.findViewById<ListView>(R.id.listViewProviderFilter)
        val btnClear = dialogView.findViewById<MaterialButton>(R.id.btnClearFilters)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnFilterCancel)
        val btnApply = dialogView.findViewById<MaterialButton>(R.id.btnFilterApply)

        val providerTitles = providers.map { it.providerName }
        val providerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, providerTitles)
        providerListView.adapter = providerAdapter
        providerListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        for (i in providers.indices) {
            providerListView.setItemChecked(i, selectedProviderFilter.contains(providers[i].providerId))
        }

        btnClear.setOnClickListener {
            selectedProviderFilter.clear()
            for (i in providers.indices) providerListView.setItemChecked(i, false)
        }

        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            selectedProviderFilter.clear()
            val provChecked = providerListView.checkedItemPositions
            for (i in providers.indices) {
                if (provChecked[i]) {
                    selectedProviderFilter.add(providers[i].providerId)
                }
            }

            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyFilters() {
        val provFilter = if (selectedProviderFilter.isEmpty()) null else selectedProviderFilter.toList()
        viewModel.loadShipments(provFilter)
    }

    private fun showDeleteConfirmation(shipmentId: Int, position: Int?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null)
        val message = dialogView.findViewById<TextView>(R.id.tvMessage)
        message.text = "Вы уверены? Количество товара будет возвращено на склад."

        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val adapter = binding.recyclerViewShipments.adapter as? ShipmentAdapter

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            if (position != null) {
                adapter?.notifyItemChanged(position)
            }
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            viewModel.deleteShipment(shipmentId)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (position != null) {
                adapter?.notifyItemChanged(position)
            }
        }

        dialog.show()
    }
}