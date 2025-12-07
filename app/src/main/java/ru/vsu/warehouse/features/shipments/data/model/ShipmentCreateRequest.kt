package ru.vsu.warehouse.features.shipments.data.model

import ru.vsu.warehouse.features.supplies.data.model.NewProviderRequest
import java.time.LocalDate

data class ShipmentCreateRequest(
    val productId: Int,
    val providerId: Int? = null,
    val newProvider: NewProviderRequest? = null,
    val shipmentQuantity: Int,
    val shipmentDate: LocalDate
)