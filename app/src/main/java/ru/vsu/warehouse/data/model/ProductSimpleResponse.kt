package ru.vsu.warehouse.data.model

data class ProductSimpleResponse(
    val productId: Int,
    val productTitle: String,
    val currentCount: Int,
    val categoryIds: List<Int>
)