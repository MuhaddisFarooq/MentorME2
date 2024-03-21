package com.example.muhaddis_i210391

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MyProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71
    private lateinit var ivBack: ImageView
    private lateinit var ivProfilePic: ImageView
    private lateinit var ivSettings: ImageView
    private lateinit var ivCoverPhoto: ImageView // Assuming you have a cover photo ImageView
    private lateinit var btnBookedSessions: Button
    private lateinit var rvFavoriteMentors: RecyclerView
    private lateinit var rvReviews: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var tvName: TextView
    private lateinit var tvCity: TextView
    private var selectedImageUri: Uri? = null
    private lateinit var mentorId: String
    private lateinit var mentorsAdapter: MentorAdapter
    private lateinit var mentorsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)


        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        // Setup RecyclerView
        // Setup RecyclerView
        rvReviews = findViewById(R.id.rvReviews)
        rvReviews.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reviewAdapter = ReviewAdapter(mutableListOf())
        rvReviews.adapter = reviewAdapter

        if (currentUser != null) {
            fetchAndDisplayReviews()
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
        }

        mentorsRecyclerView = findViewById(R.id.rvFavoriteMentors) // Assuming this ID is set for your RecyclerView
        ivBack = findViewById(R.id.ivBack)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        ivSettings = findViewById(R.id.edit)
        ivCoverPhoto = findViewById(R.id.ivCoverPhoto)
        btnBookedSessions = findViewById(R.id.btnBookedSessions)
        rvReviews = findViewById(R.id.rvReviews)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        tvName = findViewById(R.id.tvName)
        tvCity = findViewById(R.id.tvLocation)
        ivProfilePic = findViewById(R.id.ivProfilePic)




        setupClickListeners()
        setupBottomNavigationView()
        updateProfileImage()
        fetchCoverPhoto()





        fetchUserProfile()
        setupMentorsRecyclerView()





        ivSettings.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        btnBookedSessions.setOnClickListener {
            val intent = Intent(this, BookedSessionsActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }
            selectedImageUri = data.data
            ivCoverPhoto.setImageURI(selectedImageUri)
            uploadImageToFirebase()
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
            }
        )

        // Setup RecyclerViews as before
        mentorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyProfileActivity, RecyclerView.HORIZONTAL, false)
            adapter = mentorsAdapter
        }
        fetchMentorsFromFirebase()



    }
    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_profile
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToMenu()
                    true
                }

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

                else -> false
            }
        }
    }


    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        ivCoverPhoto.setOnClickListener {
            openGalleryForImage()
        }
        ivSettings.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        btnBookedSessions.setOnClickListener {
            val intent = Intent(this, BookedSessionsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(it)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@MyProfileActivity).load(imageUrl).into(ivProfilePic)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }


    private fun uploadImageToFirebase() {
        val storageReference = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val ref = storageReference.child("cover_photos/$userId.jpg")

        selectedImageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        updateCoverPhotoUrl(it.toString())
                    }
                }
                .addOnFailureListener {
                    // Handle unsuccessful uploads
                }
        }
    }

    private fun updateCoverPhotoUrl(url: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        databaseReference.child("coverPhotoUrl").setValue(url).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Cover photo updated.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update cover photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCoverPhoto() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(it)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val coverPhotoUrl = snapshot.child("coverPhotoUrl").getValue(String::class.java)
                    if (!coverPhotoUrl.isNullOrEmpty()) {
                        Glide.with(this@MyProfileActivity).load(coverPhotoUrl).into(ivCoverPhoto)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }


    private fun fetchUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value as? String
                    val city = snapshot.child("city").value as? String
                    tvName.text = name ?: "N/A"
                    tvCity.text = city ?: "N/A"
                    val imageUrl = snapshot.child("profileImageUrl").value as? String
                    imageUrl?.let {
                        Glide.with(this@MyProfileActivity).load(it).into(ivProfilePic)
                    }
                } else {
                    Toast.makeText(
                        this@MyProfileActivity,
                        "User data not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MyProfileActivity,
                    "Failed to load user data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

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

    private fun fetchAndDisplayReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("Mentors")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reviewsList = mutableListOf<Review>()
                    snapshot.children.forEach { mentorSnapshot ->
                        val mentorId = mentorSnapshot.key ?: return@forEach
                        val mentorName = mentorSnapshot.child("name").value.toString()
                        mentorSnapshot.child("Feedback").children.forEach { feedbackSnapshot ->
                            val feedbackUserId = feedbackSnapshot.child("userId").value.toString()
                            if (feedbackUserId == userId) { // Filter by current user's ID
                                val reviewText = feedbackSnapshot.child("text").value.toString()
                                val rating = feedbackSnapshot.child("rating").value.toString().toFloatOrNull() ?: 0f
                                reviewsList.add(Review(reviewText, rating, userId, mentorName, mentorId))
                            }
                        }
                    }
                    reviewAdapter.updateReviews(reviewsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Failed to load reviews.", Toast.LENGTH_SHORT).show()
                }
            })
    }


}


