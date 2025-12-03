package ru.vsu.warehouse.data.model

data class ProductResponse(
    val productId: Int,
    val productTitle: String,
    val currentCount: Int,
    val categoryTitles: List<String>
)