package ru.vsu.warehouse.data.api

import retrofit2.Response
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest
import retrofit2.http.*
import ru.vsu.warehouse.data.model.CategoryResponse
import ru.vsu.warehouse.data.model.ProductSimpleResponse
import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.data.model.ProviderSimpleResponse
import ru.vsu.warehouse.data.model.SupplyResponse
import ru.vsu.warehouse.features.providers.data.model.ProviderUpdateRequest
import ru.vsu.warehouse.features.supplies.data.model.SupplyCreateRequest

interface WarehouseApi {

    // товары
    @GET("api/products")
    suspend fun getAllProducts(
        @Query("filter") filter: String? = null,
        @Query("categoryId") categoryId: List<Int>?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<ProductResponse>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductResponse

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductUpdateRequest
    ): ProductResponse

    @GET("api/products/simple")
    suspend fun getSimpleProducts(): List<ProductSimpleResponse>

    // категории
    @GET("api/categories")
    suspend fun getAllCategories(): List<CategoryResponse>

    // поставщики
    @GET("api/providers")
    suspend fun getProviders(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PageResponse<ProviderResponse>

    @GET("api/providers/{id}")
    suspend fun getProviderById(@Path("id") id: Int): ProviderResponse

    @PUT("api/providers/{id}")
    suspend fun updateProvider(
        @Path("id") id: Int,
        @Body request: ProviderUpdateRequest
    ): ProviderResponse

    // поставкки
    @GET("api/supplies/filtered")
    suspend fun getSupplies(
        @Query("categoryIds") categoryIds: List<Int>? = null,
        @Query("providerIds") providerIds: List<Int>? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15
    ): PageResponse<SupplyResponse>

    @POST("api/supplies")
    suspend fun createSupply(@Body request: SupplyCreateRequest): SupplyResponse

    @DELETE("api/supplies/{id}")
    suspend fun deleteSupply(@Path("id") id: Int): Response<Unit>

    @GET("api/providers/simple")
    suspend fun getSimpleProviders(): List<ProviderSimpleResponse>
}