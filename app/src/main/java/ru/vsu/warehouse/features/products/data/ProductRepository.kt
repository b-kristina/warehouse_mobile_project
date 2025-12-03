package ru.vsu.warehouse.features.products.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.PageResponse
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.features.products.data.model.ProductUpdateRequest

class ProductRepository {
    private val api = RetrofitClient.api

    suspend fun getAllProducts(
        filter: String? = null,
        categoryId: List<Int>? = null,
        page: Int = 0,
        size: Int = 20
    ): PageResponse<ProductResponse> {
        return api.getAllProducts(filter, categoryId, page, size)
    }

    suspend fun getProductById(id: Int): ProductResponse {
        return api.getProductById(id)
    }

    suspend fun updateProduct(id: Int, request: ProductUpdateRequest): ProductResponse {
        return api.updateProduct(id, request)
    }
    fun getProductsPaging(
        filter: String? = null,
        categoryIds: List<Int>? = null
    ): Flow<PagingData<ProductResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ProductPagingSource(filter, categoryIds)
            }
        ).flow
    }
}