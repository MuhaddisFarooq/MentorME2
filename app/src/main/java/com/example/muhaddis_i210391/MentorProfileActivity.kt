package com.example.muhaddis_i210391

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class MentorProfileActivity : AppCompatActivity() {

    private var mentorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_profile)

        mentorId = intent.getStringExtra("MENTOR_ID")




        val mentorNameTextView: TextView = findViewById(R.id.tvMentorName)
        val mentorSpecialtyTextView: TextView = findViewById(R.id.tvMentorSpecialty)
        val mentorProfileImage: ImageView = findViewById(R.id.mentorProfileImage)
        val backButton: ImageView = findViewById(R.id.iv_back_arrow)
        val bookSessionButton: Button = findViewById(R.id.btnBookSession)
        val dropReviewButton: Button = findViewById(R.id.btnDropReview)

        backButton.setOnClickListener {
            finish()
        }

        // Fetch mentor details from Firebase using mentorId
        if (mentorId != null) {
            fetchMentorDetails(mentorId!!, mentorNameTextView, mentorSpecialtyTextView, mentorProfileImage)
        }


        bookSessionButton.setOnClickListener {
            val intent = Intent(this, BookSessionActivity::class.java)
            intent.putExtra("MENTOR_ID", mentorId)
            startActivity(intent)
        }

        dropReviewButton.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java).apply {
                putExtra("MENTOR_ID", mentorId)
            }
            startActivity(intent)
        }
    }

    private fun fetchMentorDetails(mentorId: String, mentorNameTextView: TextView, mentorSpecialtyTextView: TextView, mentorProfileImage: ImageView) {
        val mentorRef = FirebaseDatabase.getInstance().getReference("Mentors").child(mentorId)
        mentorRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val mentorName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val mentorImageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
                val mentorSpecialty = snapshot.child("description").getValue(String::class.java) ?: "Unknown Specialty"

                mentorNameTextView.text = mentorName
                mentorSpecialtyTextView.text = mentorSpecialty
                Glide.with(this@MentorProfileActivity).load(mentorImageUrl).into(mentorProfileImage)


                // Setting the About Me text based on the mentor's specialty
                val aboutMeTextView: TextView = findViewById(R.id.tvAboutMeDescription) // Assuming you have this TextView in your layout
                aboutMeTextView.text = when (mentorSpecialty) {
                    "UI/UX Designer" -> "I am a passionate UX designer at Google with a focus on creating user-centric and intuitive interfaces. With 10 years of experience, I have had the opportunity to work on diverse projects that have shaped my understanding of design principles and user experience."
                    "Software Engineer" -> "As a software engineer, I specialize in building scalable and efficient software systems."
                    else -> "I am a professional in the tech industry, dedicated to sharing my knowledge and expertise."
                }
            }

        }.addOnFailureListener {
            // Log or handle the error
        }
    }



}
