package ru.vsu.warehouse.data.model

data class ProviderResponse(
    val providerId: Int,
    val providerName: String,
    val inn: String,
    val companyName: String,
    val companyAddress: String
)