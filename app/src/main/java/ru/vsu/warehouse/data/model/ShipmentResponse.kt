package ru.vsu.warehouse.data.model

import java.time.LocalDate

data class ShipmentResponse(
    val shipmentId: Int,
    val productTitle: String,
    val providerName: String,
    val shipmentQuantity: Int,
    val shipmentDate: LocalDate
)