package ru.vsu.warehouse.data.model

import java.time.LocalDate

data class WriteOffResponse(
    val writeOffId: Int,
    val productTitle: String,
    val writeOffQuantity: Int,
    val writeOffReason: String,
    val writeOffDate: LocalDate
)