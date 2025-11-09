package ru.vsu.warehouse.features.products.data.model

data class ProductUpdateRequest(
    val productTitle: String,
    val categoryIds: List<Int>
)