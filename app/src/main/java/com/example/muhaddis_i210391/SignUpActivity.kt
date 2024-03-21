package com.example.muhaddis_i210391
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val spinnerCountry = findViewById<Spinner>(R.id.spinnerCountry)
        val spinnerCity = findViewById<Spinner>(R.id.spinnerCity)
        val buttonSignUp = findViewById<TextView>(R.id.buttonSignUp)
        val tvLoginPrompt = findViewById<TextView>(R.id.tvLogInPrompt)

        val countries = arrayOf("Select Country", "Pakistan", "Other Countries...")
        spinnerCountry.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        spinnerCountry.setSelection(0)

        val cities = arrayOf("Select City", "Multan", "Islamabad", "Lahore", "Other Cities...")
        spinnerCity.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)
        spinnerCity.setSelection(0)

        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val name = editTextName.text.toString().trim() // Get the name input
            val country = spinnerCountry.selectedItem.toString()
            val city = spinnerCity.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with creating the user account...
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let {
                        val userId = it.uid
                        // Store additional user details
                        val userDetails = hashMapOf(
                            "email" to email,
                            "name" to name, // Now including the name
                            "country" to country,
                            "city" to city
                        )
                        // Save these details in Firebase under "Users" node
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(userId)
                            .setValue(userDetails)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(baseContext, "Signup successful. User details saved.", Toast.LENGTH_SHORT).show()
                                    // Intent to navigate to the next activity
                                } else {
                                    // Handle failure
                                }
                            }
                    }
                } else {
                    // Handle sign up failure
                }
            }
        }


        tvLoginPrompt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
