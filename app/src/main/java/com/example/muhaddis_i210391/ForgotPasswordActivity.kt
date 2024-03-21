package com.example.muhaddis_i210391


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val loginTextView = findViewById<TextView>(R.id.loginTextView)

        backArrow.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {

                val intent = Intent(this, ResetPasswordActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        loginTextView.setOnClickListener {
            finish()
        }



    }
}
