package ru.vsu.warehouse.features.supplies.data.model

import java.time.LocalDate

data class SupplyCreateRequest(
    val productId: Int? = null,
    val newProductTitle: String? = null,
    val categoryIds: List<Int>? = null,
    val providerId: Int? = null,
    val newProvider: NewProviderRequest? = null,
    val supplyQuantity: Int,
    val supplyDate: LocalDate
)

data class NewProviderRequest(
    val providerName: String,
    val inn: String,
    val companyName: String,
    val companyAddress: String
)