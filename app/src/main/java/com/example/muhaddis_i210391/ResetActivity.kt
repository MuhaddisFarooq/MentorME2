package com.example.muhaddis_i210391

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)


        val textViewLogin = findViewById<TextView>(R.id.resetTextview)

        val buttonReset = findViewById<Button>(R.id.resetButton)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val confirmNewPasswordEditText = findViewById<EditText>(R.id.confirmNewPasswordEditText)

        buttonReset.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

            if (newPassword.isNotEmpty() && newPassword == confirmNewPassword) {
                Toast.makeText(this, "Password has been reset successfully", Toast.LENGTH_SHORT).show()

                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            } else {
                Toast.makeText(this, "Passwords are empty or do not match", Toast.LENGTH_SHORT).show()
            }
        }

        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
