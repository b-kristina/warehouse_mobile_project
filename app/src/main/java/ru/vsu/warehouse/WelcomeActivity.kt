package ru.vsu.warehouse

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import ru.vsu.warehouse.features.categories.view.CategoriesActivity
import ru.vsu.warehouse.features.products.view.ProductsActivity
import ru.vsu.warehouse.features.providers.view.ProvidersActivity
import ru.vsu.warehouse.features.supplies.view.SuppliesActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // кнопка "Товары"
        findViewById<Button>(R.id.btnProducts).setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        findViewById<Button>(R.id.btnCategories).setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
        findViewById<Button>(R.id.btnProviders).setOnClickListener {
            startActivity(Intent(this, ProvidersActivity::class.java))
        }
        findViewById<Button>(R.id.btnSupplies).setOnClickListener {
            startActivity(Intent(this, SuppliesActivity::class.java))
        }


        // navigation 3 реализация навигации в андройд на хабре,  material.io
    }
}