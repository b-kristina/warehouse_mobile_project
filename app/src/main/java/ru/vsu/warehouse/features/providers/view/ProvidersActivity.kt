package ru.vsu.warehouse.features.providers.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.vsu.warehouse.R
import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.databinding.ActivityProvidersBinding
import ru.vsu.warehouse.databinding.DialogEditProviderBinding

class ProvidersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProvidersBinding
    private val viewModel: ProvidersViewModel by viewModels()
    private var selectedFilter: String = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvidersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewProviders.layoutManager = LinearLayoutManager(this)

        val adapter = ProviderAdapter { provider ->
            openEditDialog(provider)
        }
        binding.recyclerViewProviders.adapter = adapter

        // Подписка на список поставщиков
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providers.collect { providers ->
                    adapter.submitList(providers)
                }
            }
        }

        // Подписка на ошибки
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@ProvidersActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Загрузка данных
        viewModel.loadProviders()
    }

    private fun openEditDialog(provider: ProviderResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_provider, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextProviderName)
        val editTextInn = dialogView.findViewById<EditText>(R.id.editTextInn)
        val editTextCompany = dialogView.findViewById<EditText>(R.id.editTextCompanyName)
        val editTextAddress = dialogView.findViewById<EditText>(R.id.editTextAddress)

        editTextName.setText(provider.providerName)
        editTextInn.setText(provider.inn)
        editTextCompany.setText(provider.companyName)
        editTextAddress.setText(provider.companyAddress)

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Редактировать поставщика")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = editTextName.text.toString().trim()
                val inn = editTextInn.text.toString().trim()
                val company = editTextCompany.text.toString().trim()
                val address = editTextAddress.text.toString().trim()

                if (name.isEmpty() || inn.isEmpty() || company.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Все поля обязательны", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (inn.length != 12 || !inn.all { it.isDigit() }) {
                    Toast.makeText(this, "ИНН должен содержать 12 цифр", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.updateProvider(provider.providerId, name, inn, company, address)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_providers, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupFilter)
        
        when (selectedFilter) {
            "IP" -> radioGroup.check(R.id.radioIP)
            "LLC" -> radioGroup.check(R.id.radioLLC)
            "OTHER" -> radioGroup.check(R.id.radioOther)
            else -> radioGroup.check(R.id.radioAll)
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("Фильтры")
            .setView(dialogView)
            .setPositiveButton("Применить") { _, _ ->
                selectedFilter = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioIP -> "IP"
                    R.id.radioLLC -> "LLC"
                    R.id.radioOther -> "OTHER"
                    else -> "all"
                }

                val filterValue = if (selectedFilter == "all") null else selectedFilter
                viewModel.loadProviders(filterValue)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    fun openFilterDialog(view: View) {
        showFilterDialog()
    }
}