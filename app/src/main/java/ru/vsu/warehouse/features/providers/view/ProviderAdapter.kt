package ru.vsu.warehouse.features.providers.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.ProviderResponse
import ru.vsu.warehouse.databinding.ItemProviderBinding

class ProviderAdapter(
    private val onItemClick: (ProviderResponse) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    private var providers = emptyList<ProviderResponse>()

    fun submitList(providers: List<ProviderResponse>) {
        this.providers = providers
        notifyDataSetChanged()
    }

    class ProviderViewHolder(val binding: ItemProviderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding = ItemProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProviderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        with(holder.binding) {
            tvProviderName.text = provider.providerName
            tvCompanyName.text = provider.companyName
            tvInn.text = "ИНН: ${provider.inn}"
            tvAddress.text = provider.companyAddress

            btnEdit.setOnClickListener {
                onItemClick(provider)
            }
        }
    }

    override fun getItemCount() = providers.size
}