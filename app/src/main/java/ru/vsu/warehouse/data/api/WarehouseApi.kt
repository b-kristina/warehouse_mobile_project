package ru.vsu.warehouse.data.api

import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest
import retrofit2.http.*
import ru.vsu.warehouse.data.model.CategoryResponse

interface WarehouseApi {

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

    @GET("api/categories")
    suspend fun getAllCategories(): List<CategoryResponse>
}