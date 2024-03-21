package com.example.muhaddis_i210391

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddMentorActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mentor)



        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val btnUploadPhoto = findViewById<Button>(R.id.btnUploadPhoto)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val etMentorName = findViewById<EditText>(R.id.etMentorName)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        btnUpload.setOnClickListener {
            uploadMentorData()
        }

        ivBack.setOnClickListener { finish() }
    }

    private fun uploadMentorData() {
        val name = findViewById<EditText>(R.id.etMentorName).text.toString().trim()
        val description = findViewById<EditText>(R.id.etDescription).text.toString().trim()
        val price = findViewById<EditText>(R.id.etPrice).text.toString().trim()

        if (name.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty() && imageUri != null) {
            uploadImageToFirebase(name, description, price)
        } else {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadImageToFirebase(name: String, description: String, price: String) {
        val fileReference = FirebaseStorage.getInstance().getReference("mentor_images/${UUID.randomUUID()}")

        imageUri?.let { uri ->
            fileReference.putFile(uri).addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    val mentor = Mentorr("", name, description, price, downloadUri.toString())
                    addMentorToFirebase(mentor)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
        }
    }

    private fun addMentorToFirebase(mentor: Mentorr) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Mentors")

        val mentorId = databaseReference.push().key

        if (mentorId == null) {
            Toast.makeText(this, "Error generating mentor ID", Toast.LENGTH_SHORT).show()
            return
        }


        val mentorWithId = mentor.copy(id = mentorId)

        databaseReference.child(mentorId).setValue(mentorWithId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Mentor added successfully", Toast.LENGTH_SHORT).show()
                createNotificationForNewMentor(mentorWithId)
            } else {
                Toast.makeText(this, "Failed to add mentor", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun createNotificationForNewMentor(mentor: Mentorr) {
        val notification = mapOf(
            "id" to UUID.randomUUID().toString(),
            "title" to "New Mentor Added",
            "message" to "${mentor.name} is now available for mentoring!",
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("notifications").push()
            .setValue(notification)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("AddMentorActivity", "Failed to create notification for new mentor")
                }
            }
    }


}
