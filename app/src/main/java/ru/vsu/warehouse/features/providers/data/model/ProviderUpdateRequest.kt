package ru.vsu.warehouse.features.providers.data.model

data class ProviderUpdateRequest(
    val providerName: String,
    val inn: String,
    val companyName: String,
    val companyAddress: String
)