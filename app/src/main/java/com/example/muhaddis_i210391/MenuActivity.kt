package com.example.muhaddis_i210391

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class MenuActivity : AppCompatActivity() {

    private lateinit var mentorsRecyclerView: RecyclerView
    private lateinit var mentorsRecyclerView2: RecyclerView
    private lateinit var mentorsRecyclerView3: RecyclerView

    private lateinit var notificationIcon: ImageView
    private lateinit var greetingTextView: TextView
    private lateinit var mentorsAdapter: MentorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        notificationIcon = findViewById(R.id.notificationIcon)
        greetingTextView = findViewById(R.id.greetingTextView)
        mentorsRecyclerView = findViewById(R.id.mentorsRecyclerView)
        mentorsRecyclerView2= findViewById(R.id.mentorsRecyclerView2)
        mentorsRecyclerView3= findViewById(R.id.mentorsRecyclerView3)

        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        fetchUserName()
        setupMentorsRecyclerView()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddMentorActivity::class.java))
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }


        private fun setupMentorsRecyclerView() {
        mentorsAdapter = MentorAdapter(
            emptyList(),
            onItemClicked = { mentor ->
                val intent = Intent(this, MentorProfileActivity::class.java).apply {
                    putExtra("MENTOR_ID", mentor.id)
                }
                startActivity(intent)
            },
            onFavoriteClicked = { mentor ->
                updateMentorFavoriteStatus(mentor)
            }
        )

        // Setup RecyclerViews as before
        mentorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, RecyclerView.HORIZONTAL, false)
            adapter = mentorsAdapter
        }
        mentorsRecyclerView2.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, RecyclerView.HORIZONTAL, false)
            adapter = mentorsAdapter
        }
        mentorsRecyclerView3.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, RecyclerView.HORIZONTAL, false)
            adapter = mentorsAdapter
        }

        fetchMentorsFromFirebase()
    }


    private fun fetchMentorsFromFirebase() {
        val mentorsReference = FirebaseDatabase.getInstance().getReference("Mentors")
        mentorsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mentorsList = dataSnapshot.children.mapNotNull { it.getValue(Mentorr::class.java) }
                mentorsAdapter.updateData(mentorsList) // Update the adapter's data
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun fetchUserName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                greetingTextView.text = "Hello, ${name ?: "User"}"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateMentorFavoriteStatus(mentor: Mentorr) {
        val newStatus = !mentor.isFavorite
        val mentorRef = FirebaseDatabase.getInstance().getReference("Mentors").child(mentor.id)
        mentorRef.child("isFavorite").setValue(newStatus).addOnSuccessListener {
            Log.d("MenuActivity", "Successfully updated favorite status for ${mentor.name}")
            if (newStatus) { // If the mentor is now a favorite
                sendFavoriteNotification(mentor)
            }
        }.addOnFailureListener {
            Log.e("MenuActivity", "Failed to update favorite status for ${mentor.name}", it)
        }
    }

    private fun sendFavoriteNotification(mentor: Mentorr) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationRef = FirebaseDatabase.getInstance().getReference("Notifications")
        val query = notificationRef.orderByChild("message").equalTo("You have favorited ${mentor.name}")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("MenuActivity", "Notification already exists for favoriting ${mentor.name}")
                } else {
                    val newNotificationRef = notificationRef.push()
                    val notification = mapOf(
                        "message" to "You have favorited ${mentor.name}",
                        "userId" to userId // Ensure notifications are user-specific if required
                    )
                    newNotificationRef.setValue(notification).addOnSuccessListener {
                        Log.d("MenuActivity", "Notification sent for favoriting ${mentor.name}")
                        // Start NotificationsActivity here
                        val intent = Intent(this@MenuActivity, NotificationsActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener {
                        Log.e("MenuActivity", "Failed to send notification for favoriting ${mentor.name}", it)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MenuActivity", "Error checking existing notifications", databaseError.toException())
            }
        })
    }

}
