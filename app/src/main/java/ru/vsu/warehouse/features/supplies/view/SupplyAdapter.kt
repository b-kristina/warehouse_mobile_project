package ru.vsu.warehouse.features.supplies.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.SupplyResponse
import ru.vsu.warehouse.databinding.ItemSupplyBinding
import java.time.format.DateTimeFormatter

class SupplyAdapter(
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<SupplyAdapter.SupplyViewHolder>() {

    private var supplies = emptyList<SupplyResponse>()

    fun submitList(supplies: List<SupplyResponse>) {
        this.supplies = supplies
        notifyDataSetChanged()
    }

    class SupplyViewHolder(val binding: ItemSupplyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplyViewHolder {
        val binding = ItemSupplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplyViewHolder, position: Int) {
        val supply = supplies[position]
        with(holder.binding) {
            tvSupplyDate.text = "Дата: ${supply.supplyDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            tvProductTitle.text = supply.productTitle
            tvCategories.text = supply.categoryTitles.joinToString(", ") { it }
            tvProviderName.text = supply.providerName
            tvQuantity.text = "Количество: ${supply.supplyQuantity}"

            btnDelete.setOnClickListener {
                onDeleteClick(supply.supplyId)
            }
        }
    }

    override fun getItemCount() = supplies.size
}