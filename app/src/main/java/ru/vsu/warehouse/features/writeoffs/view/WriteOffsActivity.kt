package ru.vsu.warehouse.features.writeoffs.view

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
import ru.vsu.warehouse.data.model.WriteOffResponse
import ru.vsu.warehouse.databinding.ActivityWriteOffsBinding
import ru.vsu.warehouse.features.writeoffs.data.model.WriteOffCreateRequest
import ru.vsu.warehouse.utils.SwipeToDeleteCallback
import java.time.LocalDate

class WriteOffsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteOffsBinding
    private val viewModel: WriteOffViewModel by viewModels()

    private var products: List<ProductSimpleResponse> = emptyList()

    // Стандартные причины
    private val standardReasons = listOf(
        "Брак",
        "Повреждение при транспортировке",
        "Истек срок годности",
        "Кража",
        "Списание по акту"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteOffsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewWriteOffs.layoutManager = LinearLayoutManager(this)

        val adapter = WriteOffAdapter { writeOffId ->
            showDeleteConfirmation(writeOffId, null)
        }
        binding.recyclerViewWriteOffs.adapter = adapter

        val swipeToDelete = SwipeToDeleteCallback(this) { position ->
            val writeOff = adapter.getWriteOffAt(position)
            showDeleteConfirmation(writeOff.writeOffId, position)
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerViewWriteOffs)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.writeOffs.collect { writeOffs ->
                    adapter.submitList(writeOffs)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { products ->
                    this@WriteOffsActivity.products = products
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@WriteOffsActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.loadWriteOffs()
    }

    fun openNewWriteOffDialog(view: View) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_write_off, null)
        val spinnerProduct = dialogView.findViewById<Spinner>(R.id.spinnerProduct)
        val spinnerReason = dialogView.findViewById<Spinner>(R.id.spinnerReason)
        val wrapperCustomReason = dialogView.findViewById<TextInputLayout>(R.id.wrapperCustomReason)
        val editTextCustomReason = dialogView.findViewById<EditText>(R.id.editTextCustomReason)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)

        val today = LocalDate.now().toString()
        editTextDate.setText(today)

        // Товары
        val productTitles = listOf("Выберите товар") + products.map { "${it.productTitle} (остаток: ${it.currentCount})" }
        val productAdapter = ArrayAdapter(this, R.layout.spinner_item, productTitles)
        productAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerProduct.adapter = productAdapter

        // Причины
        val reasonTitles = listOf("Выберите причину", "Брак", "Повреждение при транспортировке", "Истек срок годности", "Кража", "Списание по акту", "Другое")
        val reasonAdapter = ArrayAdapter(this, R.layout.spinner_item, reasonTitles)
        reasonAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerReason.adapter = reasonAdapter

        spinnerReason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                wrapperCustomReason.visibility = if (position == 6) View.VISIBLE else View.GONE
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
            // Валидация товара
            if (spinnerProduct.selectedItemPosition == 0) {
                Toast.makeText(this, "Выберите товар", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val productId = products[spinnerProduct.selectedItemPosition - 1].productId
            val selectedProduct = products[spinnerProduct.selectedItemPosition - 1]

            // Валидация количества
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

            // Валидация причины
            var reason = ""
            when (spinnerReason.selectedItemPosition) {
                0 -> {
                    Toast.makeText(this, "Выберите причину списания", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                6 -> {
                    reason = editTextCustomReason.text.toString().trim()
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Введите причину списания", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                else -> {
                    reason = reasonTitles[spinnerReason.selectedItemPosition]
                }
            }

            // Валидация даты
            val dateStr = editTextDate.text.toString().trim()
            val date = try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                Toast.makeText(this, "Неверный формат даты (гггг-мм-дд)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = WriteOffCreateRequest(
                productId = productId,
                writeOffQuantity = quantity,
                writeOffReason = reason,
                writeOffDate = date
            )

            viewModel.createWriteOff(request)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun openFilterDialog(view: View) {
        showFilterDialog()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_write_offs, null)
        val listViewReasons = dialogView.findViewById<ListView>(R.id.listViewReasons)
        val checkBoxNonStandard = dialogView.findViewById<CheckBox>(R.id.checkBoxNonStandard)
        val btnClear = dialogView.findViewById<MaterialButton>(R.id.btnClearFilters)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnFilterCancel)
        val btnApply = dialogView.findViewById<MaterialButton>(R.id.btnFilterApply)

        val reasonAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, standardReasons)
        listViewReasons.adapter = reasonAdapter
        listViewReasons.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Обработка нажатий на причины
        listViewReasons.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            // Снимаем галочку "Нестандартные", если выбраны только стандартные
            checkBoxNonStandard.isChecked = false
        }

        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnClear.setOnClickListener {
            for (i in standardReasons.indices) {
                listViewReasons.setItemChecked(i, false)
            }
            checkBoxNonStandard.isChecked = false
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            val selectedReasons = mutableListOf<String>()
            val checked = listViewReasons.checkedItemPositions
            for (i in standardReasons.indices) {
                if (checked[i]) {
                    selectedReasons.add(standardReasons[i])
                }
            }
            val includeNonStandard = checkBoxNonStandard.isChecked

            viewModel.loadWriteOffs(
                reasons = if (selectedReasons.isEmpty() && !includeNonStandard) null else selectedReasons,
                includeNonStandard = includeNonStandard
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(writeOffId: Int, position: Int?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null)
        val message = dialogView.findViewById<TextView>(R.id.tvMessage)
        message.text = "Вы уверены? Количество товара будет возвращено на склад."

        val dialog = AlertDialog.Builder(this, R.style.DialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val adapter = binding.recyclerViewWriteOffs.adapter as? WriteOffAdapter

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            if (position != null) {
                adapter?.notifyItemChanged(position)
            }
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            viewModel.deleteWriteOff(writeOffId)
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