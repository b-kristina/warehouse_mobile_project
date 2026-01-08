package ru.vsu.warehouse.features.writeoffs.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.vsu.warehouse.data.model.WriteOffResponse
import ru.vsu.warehouse.databinding.ItemWriteOffBinding
import java.time.format.DateTimeFormatter

class WriteOffAdapter(
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<WriteOffAdapter.WriteOffViewHolder>() {

    private var writeOffs = emptyList<WriteOffResponse>()

    fun submitList(writeOffs: List<WriteOffResponse>) {
        this.writeOffs = writeOffs
        notifyDataSetChanged()
    }

    class WriteOffViewHolder(val binding: ItemWriteOffBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WriteOffViewHolder {
        val binding = ItemWriteOffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WriteOffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WriteOffViewHolder, position: Int) {
        val writeOff = writeOffs[position]
        with(holder.binding) {
            tvWriteOffDate.text = "Дата: ${writeOff.writeOffDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            tvProductTitle.text = writeOff.productTitle
            tvReason.text = writeOff.writeOffReason
            tvQuantity.text = "Количество: ${writeOff.writeOffQuantity}"
        }
    }

    override fun getItemCount() = writeOffs.size

    fun getWriteOffAt(position: Int): WriteOffResponse {
        return writeOffs[position]
    }
}