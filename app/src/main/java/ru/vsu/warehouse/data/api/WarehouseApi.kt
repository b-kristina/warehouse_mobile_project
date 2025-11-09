package ru.vsu.warehouse.data.api

import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.Product
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface WarehouseApi {

    @GET("api/products")
    suspend fun getAllProducts(
        @Query("filter") filter: String? = null,
        @Query("categoryId") categoryId: List<Int>?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PageResponse<Product>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductUpdateRequest
    ): Product
}