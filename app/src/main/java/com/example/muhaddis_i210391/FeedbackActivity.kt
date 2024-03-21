package com.example.muhaddis_i210391
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class FeedbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback)


        val feedbackEditText: EditText = findViewById(R.id.etReviewInput)
        val ratingBar: RatingBar = findViewById(R.id.ratingBar)
        val submitButton: Button = findViewById(R.id.btnSubmitFeedback)


        val backButton: ImageView = findViewById(R.id.ivBack)
        backButton.setOnClickListener { finish() }

        val mentorNameTextView: TextView =
            findViewById(R.id.tvMentorName) // Assuming you have this TextView in your layout
        val mentorProfileImageView: ImageView = findViewById(R.id.mentorProfileImage) // Assuming you have this ImageView in your layout

        // Fetch and display mentor details
        val mentorId = intent.getStringExtra("MENTOR_ID")
        if (mentorId != null) {
            val mentorRef = FirebaseDatabase.getInstance().getReference("Mentors").child(mentorId)
            mentorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val mentorName =
                            snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val mentorImageUrl =
                            snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                        mentorNameTextView.text = mentorName
                        Glide.with(this@FeedbackActivity).load(mentorImageUrl)
                            .into(mentorProfileImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@FeedbackActivity,
                        "Error fetching mentor details.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "Mentor ID is missing.", Toast.LENGTH_SHORT).show()
        }



        submitButton.setOnClickListener {
            val feedbackText = feedbackEditText.text.toString().trim()
            val rating = ratingBar.rating

            if (feedbackText.isNotEmpty() && mentorId != null) {
                submitFeedback(mentorId, feedbackText, rating)
            } else {
                Toast.makeText(this, "Feedback cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }



    }


    private fun submitFeedback(mentorId: String, feedbackText: String, rating: Float) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val feedbackRef = FirebaseDatabase.getInstance().getReference("Mentors/$mentorId/Feedback").push()
        val feedback = mapOf(
            "text" to feedbackText,
            "rating" to rating,
            "userId" to userId  // Include the userId in the feedback
        )

        feedbackRef.setValue(feedback).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
