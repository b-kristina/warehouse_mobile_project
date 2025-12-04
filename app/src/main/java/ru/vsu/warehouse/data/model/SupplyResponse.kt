package ru.vsu.warehouse.data.model

import java.time.LocalDate

data class SupplyResponse(
    val supplyId: Int,
    val productTitle: String,
    val categoryTitles: List<String>,
    val providerName: String,
    val supplyQuantity: Int,
    val supplyDate: LocalDate
)