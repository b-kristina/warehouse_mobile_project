package ru.vsu.warehouse.features.products.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.Product
import ru.vsu.warehouse.databinding.ItemProductBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        with(holder.binding) {
            tvTitle.text = product.productTitle
            tvCategories.text = product.categoryTitles.joinToString(", ")
            tvCount.text = "Количество: ${product.currentCount}"
        }
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount() = products.size
}