package com.example.muhaddis_i210391

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class MentorAdapter(
    private var mentors: List<Mentorr>,
    private val onItemClicked: (Mentorr) -> Unit,
    private val onFavoriteClicked: (Mentorr) -> Unit


) : RecyclerView.Adapter<MentorAdapter.MentorViewHolder>() {

    inner class MentorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivMentorImage)
        val nameTextView: TextView = view.findViewById(R.id.tvMentorName)
        val descriptionTextView: TextView = view.findViewById(R.id.tvMentorDescription)
        val priceTextView: TextView = view.findViewById(R.id.tvMentorPrice)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)




    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mentor, parent, false)
        return MentorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        val mentor = mentors[position]


        // Set the favorite icon based on isFavorite status
        holder.ivFavorite.setImageResource(
            if (mentor.isFavorite) R.mipmap.heart_foreground else R.drawable.heart
        )

        holder.ivFavorite.setOnClickListener {
            onFavoriteClicked(mentor)
            mentor.toggleFavorite()
            notifyItemChanged(position)
            updateMentorInFirebase(mentor)
        }

        holder.itemView.setOnClickListener { onItemClicked(mentor) } // Handle click event

        Glide.with(holder.imageView.context)
            .load(mentor.imageUrl)
            .into(holder.imageView)
        holder.nameTextView.text = mentor.name
        holder.descriptionTextView.text = mentor.description
        holder.priceTextView.text = mentor.price
    }

    override fun getItemCount(): Int = mentors.size

    fun updateData(newData: List<Mentorr>) {
        mentors = newData
        notifyDataSetChanged()
    }

    private fun updateMentorInFirebase(mentor: Mentorr) {
        FirebaseDatabase.getInstance().getReference("Mentors").child(mentor.id)
            .child("isFavorite").setValue(mentor.isFavorite)
            .addOnSuccessListener {
                Log.d("MentorAdapter", "Mentor favorite status updated.")
            }
            .addOnFailureListener {
                Log.e("MentorAdapter", "Failed to update mentor favorite status.", it)
            }
    }

}
