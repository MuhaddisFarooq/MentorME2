package com.example.muhaddis_i210391

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID


class BookSessionActivity : AppCompatActivity() {

    private lateinit var selectedDate: String
    private lateinit var selectedTimeSlotButton: Button // Changed to store the selected Button directly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_session)

        // Initialize views
        val calendarView: CalendarView = findViewById(R.id.CalendarView)
        val ivBackArrow: ImageView = findViewById(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener { finish() }

        val timeSlotButtons: List<Button> = listOf(
            findViewById(R.id.btnTimeSlot1),
            findViewById(R.id.btnTimeSlot2),
            findViewById(R.id.btnTimeSlot3)
        )
        val btnBookAppointment: Button = findViewById(R.id.btnBookAppointment)
        val mentorId = intent.getStringExtra("MENTOR_ID") ?: return // Return early if no mentor ID

        if (mentorId != null) {
            fetchMentorDetails(mentorId)
        } else {
            Toast.makeText(this, "Mentor not specified.", Toast.LENGTH_SHORT).show()
        }

        // Set up calendar view to save selected date
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Note: month is zero-based
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        }

        // Initialize all time slots to the normal state
        timeSlotButtons.forEach { it.setBackgroundColor(getColor(R.color.timeSlotNormal)) }

        // Set up time slot buttons
        timeSlotButtons.forEach { button ->
            button.setOnClickListener { view ->
                if (this::selectedTimeSlotButton.isInitialized) {
                    selectedTimeSlotButton.setBackgroundColor(getColor(R.color.timeSlotNormal)) // Reset previous selection
                }
                selectedTimeSlotButton = view as Button
                selectedTimeSlotButton.setBackgroundColor(getColor(R.color.timeSlotSelected)) // Highlight new selection
            }
        }

        // Handle booking appointment on button click
        btnBookAppointment.setOnClickListener {
            if (this::selectedDate.isInitialized && this::selectedTimeSlotButton.isInitialized) {
                val selectedTimeSlot = selectedTimeSlotButton.text.toString()
                bookAppointment(mentorId, selectedDate, selectedTimeSlot)
            } else {
                Toast.makeText(this, "Please select a date and time slot.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bookAppointment(mentorId: String, date: String, timeSlot: String) {
        val appointmentInfo = mapOf(
            "date" to date,
            "timeSlot" to timeSlot
        )

        val bookingRef = FirebaseDatabase.getInstance().getReference("Bookings").child(mentorId).push()
        bookingRef.setValue(appointmentInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                val mentorName = findViewById<TextView>(R.id.tvMentorName).text.toString()
                createNotificationForBooking(mentorName, date, timeSlot)
                finish()
            } else {
                Toast.makeText(this, "Failed to book appointment.", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun fetchMentorDetails(mentorId: String) {
        val mentorRef = FirebaseDatabase.getInstance().getReference("Mentors").child(mentorId)
        mentorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val sessionRate = snapshot.child("price").getValue(String::class.java)
                    val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)

                    val tvMentorName: TextView = findViewById(R.id.tvMentorName)
                    val tvSessionRate: TextView = findViewById(R.id.tvSessionRate)
                    val mentorProfileImage: ImageView = findViewById(R.id.mentorProfileImage)

                    tvMentorName.text = name ?: "Mentor"
                    tvSessionRate.text = sessionRate ?: "$0/Session"
                    if (imageUrl != null) {
                        Glide.with(this@BookSessionActivity).load(imageUrl).into(mentorProfileImage)
                    }
                } else {
                    Toast.makeText(this@BookSessionActivity, "Mentor data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookSessionActivity, "Failed to load mentor details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createNotificationForBooking(mentorName: String, date: String, timeSlot: String) {
        val notificationMessage = "Your appointment with $mentorName on $date at $timeSlot is confirmed."

        val notification = mapOf(
            "id" to UUID.randomUUID().toString(),
            "title" to "Booking Confirmed",
            "message" to notificationMessage,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("notifications").push()
            .setValue(notification)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("BookSessionActivity", "Failed to create notification for booking")
                }
            }
    }

}
