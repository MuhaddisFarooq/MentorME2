package com.example.muhaddis_i210391


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class SearchResultsAdapter(
    private var items: List<Mentorr>,
    private val onMentorClicked: (Mentorr) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvMentorName)
        val descriptionTextView: TextView = view.findViewById(R.id.tvMentorDescription)
        val priceTextView: TextView = view.findViewById(R.id.tvMentorPrice)
        val mentorImageView: ImageView = view.findViewById(R.id.ivMentorImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.descriptionTextView.text = item.description
        holder.priceTextView.text = item.price
        Glide.with(holder.mentorImageView.context).load(item.imageUrl).into(holder.mentorImageView)

        // Set the click listener
        holder.itemView.setOnClickListener {
            onMentorClicked(item) // Invoke the click listener, passing the clicked item
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newData: List<Mentorr>) {
        items = newData
        notifyDataSetChanged()
    }
}
