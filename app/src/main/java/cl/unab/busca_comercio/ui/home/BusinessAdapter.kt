package cl.unab.busca_comercio.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Business

class BusinessAdapter(
    private var items: List<Business> = emptyList(),
    private val onItemClick: (Business) -> Unit = {},
    private val onItemLongClick: (Business) -> Unit = {}
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    inner class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvBusinessName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvBusinessCategory)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvBusinessAddress)

        fun bind(item: Business) {
            tvName.text = item.name.ifBlank { "Sin nombre" }
            tvCategory.text = item.category.ifBlank { "Sin rubro" }

            // Solo usamos address, para evitar el campo 'commune' que no existe
            val addressText = item.address.ifBlank { "" }

            tvAddress.text = if (addressText.isBlank()) {
                "Sin dirección"
            } else {
                addressText
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business, parent, false)
        return BusinessViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Business>) {
        items = newItems
        notifyDataSetChanged()
    }
}
