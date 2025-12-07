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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.ProviderSimpleResponse
import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.databinding.ActivityShipmentsBinding
import ru.vsu.warehouse.features.supplies.data.model.NewProviderRequest
import ru.vsu.warehouse.features.shipments.data.model.ShipmentCreateRequest
import java.time.LocalDate

class ShipmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShipmentsBinding
    private val viewModel: ShipmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShipmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewShipments.layoutManager = LinearLayoutManager(this)

        val adapter = ShipmentAdapter { shipmentId ->
            showDeleteConfirmation(shipmentId)
        }
        binding.recyclerViewShipments.adapter = adapter

        // Подписка на список отгрузок
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shipments.collect { shipments ->
                    adapter.submitList(shipments)
                }
            }
        }

        // Подписка на ошибки
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@ShipmentsActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Загрузка данных
        viewModel.loadShipments()
    }

    fun openNewShipmentDialog(view: View) {
        // TODO: реализация формы создания отгрузки
        Toast.makeText(this, "Создание отгрузки — в разработке", Toast.LENGTH_SHORT).show()
    }

    fun openFilterDialog(view: View) {
        // TODO: реализация фильтрации
        Toast.makeText(this, "Фильтрация — в разработке", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation(shipmentId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены? Количество товара будет возвращено на склад.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteShipment(shipmentId)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}