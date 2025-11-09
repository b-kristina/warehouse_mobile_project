//package ru.vsu.warehouse.app
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import ru.vsu.warehouse.databinding.ActivityMainBinding
//import ru.vsu.warehouse.features.products.data.ProductRepository
//import ru.vsu.warehouse.features.products.view.ProductAdapter
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private val productRepository = ProductRepository()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
//
//        loadProducts()
//    }
//
//    private fun loadProducts() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val products = productRepository.getAllProducts()
//                android.util.Log.d("LOAD", "Успех! Получено ${products.size} товаров")
//                withContext(Dispatchers.Main) {
//                    binding.recyclerViewProducts.adapter = ProductAdapter(products)
//                }
//            } catch (e: Exception) {
//                android.util.Log.e("LOAD", "Ошибка загрузки", e)
//                withContext(Dispatchers.Main) {
//                    // Временно покажем ошибку в UI
//                    android.widget.Toast.makeText(
//                        this@MainActivity,
//                        "Ошибка: ${e.message}",
//                        android.widget.Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }
//}