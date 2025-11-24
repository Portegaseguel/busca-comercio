package cl.unab.busca_comercio.ui.business

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.unab.busca_comercio.R
import cl.unab.busca_comercio.data.model.Rating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RatingAdapter(
    private var items: List<Rating>
) : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {

    inner class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvStars: TextView = itemView.findViewById(R.id.tvStars)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(item: Rating) {
            val nameToShow = item.userName.ifBlank { "Usuario" }
            tvUserName.text = nameToShow

            val fullStar = "★"
            val emptyStar = "☆"
            val starsClamped = item.stars.coerceIn(1, 5)
            tvStars.text = fullStar.repeat(starsClamped) + emptyStar.repeat(5 - starsClamped)

            tvComment.text = item.comment?.takeIf { it.isNotBlank() } ?: "Sin comentario"

            if (item.createdAt > 0L) {
                val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                tvDate.text = df.format(Date(item.createdAt))
                tvDate.visibility = View.VISIBLE
            } else {
                tvDate.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rating_comment, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Rating>) {
        items = newItems
        notifyDataSetChanged()
    }
}


