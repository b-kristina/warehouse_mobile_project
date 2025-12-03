package ru.vsu.warehouse.features.products.data

import android.graphics.pdf.LoadParams
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.vsu.warehouse.data.api.RetrofitClient
import ru.vsu.warehouse.data.model.ProductResponse

class ProductPagingSource(
    private val filter: String? = null,
    private val categoryIds: List<Int>? = null
) : PagingSource<Int, ProductResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductResponse> {
        return try {
            val page = params.key ?: 0
            val size = params.loadSize

            val response = RetrofitClient.api.getAllProducts(
                filter = filter,
                categoryId = categoryIds,
                page = page,
                size = size
            )

            LoadResult.Page(
                data = response.content,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.content.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProductResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}