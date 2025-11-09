package ru.vsu.warehouse.data.model

data class Product(
    val productId: Int,
    val productTitle: String,
    val currentCount: Int,
    val categoryTitles: List<String>
)