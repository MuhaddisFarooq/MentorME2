package com.example.muhaddis_i210391


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private var reviews: MutableList<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mentorNameTextView: TextView = view.findViewById(R.id.tvMentorName) // Correct ID
        val reviewTextTextView: TextView = view.findViewById(R.id.tvReviewText)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }


    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.mentorNameTextView.text = review.mentorName // Display the mentor's name
        holder.reviewTextTextView.text = review.reviewText
        holder.ratingBar.rating = review.rating
    }

    override fun getItemCount(): Int = reviews.size

    fun updateReviews(newReviews: MutableList<Review>) {
        Log.d("ReviewAdapter", "Updating reviews with ${newReviews.size} items.")
        reviews = newReviews // Directly assign the new list to support efficient updates
        notifyDataSetChanged()
    }
}
