package com.example.muhaddis_i210391

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {

    private lateinit var ivBackArrow: ImageView
    private lateinit var ivProfileIcon: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContact: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var btnUpdateProfile: Button
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 71
    private val storageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        ivBackArrow = findViewById(R.id.ivBackArrow)
        ivProfileIcon = findViewById(R.id.ivProfileIcon)
        etName = findViewById(R.id.editTextName)
        etEmail = findViewById(R.id.editTextEmail)
        etContact = findViewById(R.id.editTextContact)
        spinnerCountry = findViewById(R.id.spinnerCountry)
        spinnerCity = findViewById(R.id.spinnerCity)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)

        ivBackArrow.setOnClickListener { finish() }

        ivProfileIcon.setOnClickListener { launchImageChooser() }

        btnUpdateProfile.setOnClickListener {
            uploadImage()
            // Optionally call updateProfile() here or inside uploadImage() on success
        }

        val countries = arrayOf("Select Country", "Pakistan", "Other Countries...")
        spinnerCountry.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        spinnerCountry.setSelection(0)

        val cities = arrayOf("Select City", "Multan", "Islamabad", "Lahore", "Other Cities...")
        spinnerCity.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)
        spinnerCity.setSelection(0)
    }

    private fun launchImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            ivProfileIcon.setImageURI(filePath)
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref = storageReference.child("images/${UUID.randomUUID()}")
            val uploadTask = ref.putFile(filePath!!)

            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    updateProfile(downloadUri.toString())
                } else {
                    // Handle failures
                    Toast.makeText(this, "Upload failed: " + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfile(imageUrl: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        val userUpdates = hashMapOf<String, Any>(
            "name" to etName.text.toString(),
            "email" to etEmail.text.toString(),
            "contact" to etContact.text.toString(),
            "country" to spinnerCountry.selectedItem.toString(),
            "city" to spinnerCity.selectedItem.toString()
        )
        imageUrl?.let {
            userUpdates["profileImageUrl"] = it
        }

        databaseReference.updateChildren(userUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@EditProfileActivity, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                finish() // Optional: Close EditProfileActivity after successful update
            } else {
                Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
