package ru.vsu.warehouse.features.products.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.ProductResponse
import ru.vsu.warehouse.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (ProductResponse) -> Unit
) : PagingDataAdapter<ProductResponse, ProductAdapter.ProductViewHolder>(PRODUCT_COMPARATOR) {

    class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        if (product != null) {
            with(holder.binding) {
                tvTitle.text = product.productTitle
                tvCategories.text = product.categoryTitles.joinToString(", ")
                tvCount.text = "Количество: ${product.currentCount}"

            }
        }
    }

    companion object {
        private val PRODUCT_COMPARATOR = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(oldItem: ProductResponse, newItem: ProductResponse): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: ProductResponse, newItem: ProductResponse): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun getProductAt(position: Int): ProductResponse {
        return getItem(position) ?: throw IllegalStateException("Item not found at $position")
    }
}