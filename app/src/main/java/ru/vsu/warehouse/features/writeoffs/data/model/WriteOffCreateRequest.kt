package ru.vsu.warehouse.features.writeoffs.data.model

import java.time.LocalDate

data class WriteOffCreateRequest(
    val productId: Int,
    val writeOffQuantity: Int,
    val writeOffReason: String,
    val writeOffDate: LocalDate
)