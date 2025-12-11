package ru.vsu.warehouse.features.shipments.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.ShipmentResponse
import ru.vsu.warehouse.databinding.ItemShipmentBinding
import java.time.format.DateTimeFormatter

class ShipmentAdapter(
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder>() {

    private var shipments = emptyList<ShipmentResponse>()

    fun submitList(shipments: List<ShipmentResponse>) {
        this.shipments = shipments
        notifyDataSetChanged()
    }

    class ShipmentViewHolder(val binding: ItemShipmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipmentViewHolder {
        val binding = ItemShipmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShipmentViewHolder, position: Int) {
        val shipment = shipments[position]
        with(holder.binding) {
            tvShipmentDate.text = "Дата: ${shipment.shipmentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            tvProductTitle.text = shipment.productTitle
            tvProviderName.text = shipment.providerName
            tvQuantity.text = "Количество: ${shipment.shipmentQuantity}"
        }
    }

    override fun getItemCount() = shipments.size

    fun getShipmentAt(position: Int): ShipmentResponse {
        return shipments[position]
    }
}