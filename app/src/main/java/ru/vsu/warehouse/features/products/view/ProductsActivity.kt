package ru.vsu.warehouse.features.products.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.vsu.warehouse.databinding.ActivityMainBinding

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)

        // Подписка на StateFlow — безопасно и по-новому
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { products ->
                    binding.recyclerViewProducts.adapter = ProductAdapter(products) { /* TODO */ }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    if (!errorMsg.isNullOrBlank()) {
                        Toast.makeText(this@ProductsActivity, "Ошибка: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.loadProducts()
    }
}