package com.example.muhaddis_i210391

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OTPVerificationActivity : AppCompatActivity() {
    private lateinit var otpInputFields: Array<EditText>
    private lateinit var textViewTimer: TextView
    private lateinit var textViewResendCode: TextView
    private var countDownTimer: CountDownTimer? = null
    private var remainingTime = 20 * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otpInputFields = arrayOf(
            findViewById(R.id.editTextOtp1),
            findViewById(R.id.editTextOTP2),
            findViewById(R.id.editTextOTP3),
            findViewById(R.id.editTextOTP4),
            findViewById(R.id.editTextOTP5),
            findViewById(R.id.editTextOTP6)
        )
        textViewTimer = findViewById(R.id.textViewTimer)
        textViewResendCode = findViewById(R.id.textViewResendCode)

        startCountdownTimer()

        findViewById<TextView>(R.id.buttonVerify).setOnClickListener {
            Toast.makeText(this, "Verify Button Pressed", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }


        textViewResendCode.setOnClickListener {
            resendOTP()
            resetCountdownTimer()
        }




    }

    private fun getEnteredOTP(): String {
        return otpInputFields.joinToString("") { it.text.toString() }
    }

    private fun isValidOTP(enteredOTP: String): Boolean {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val expectedOTP = sharedPrefs.getString("OTP", "123456") // Default OTP for testing
        return enteredOTP == expectedOTP
    }

    private fun resendOTP() {

        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("OTP", "123456")
            apply()
        }
        Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show()
    }

    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(remainingTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished.toInt()
                updateTimerText()
            }

            override fun onFinish() {
                textViewTimer.text = getString(R.string.resend_code_timer_finished)
                textViewResendCode.isEnabled = true
            }
        }.start()
        textViewResendCode.isEnabled = false
    }

    fun onKeypadClick(view: View) {
        val button = view as? Button
        button?.let {
            appendNumberToOtpField(it.text.toString())
        }
    }

    fun onClearClick(view: View) {
        clearLastOtpNumber()
    }

    private fun appendNumberToOtpField(number: String) {
        for (editText in otpInputFields) {
            if (editText.text.isEmpty()) {
                editText.setText(number)
                editText.requestFocus()
                break
            }
        }
    }

    private fun clearLastOtpNumber() {
        val reversedFields = otpInputFields.reversed()
        for (editText in reversedFields) {
            if (editText.text.isNotEmpty()) {
                editText.setText("")
                editText.requestFocus()
                break
            }
        }
    }





    private fun resetCountdownTimer() {
        countDownTimer?.cancel()
        remainingTime = 20 * 1000
        startCountdownTimer()
    }

    private fun updateTimerText() {
        val minutes = remainingTime / 60000
        val seconds = (remainingTime % 60000) / 1000
        textViewTimer.text = String.format("Resend code in %02d:%02d", minutes, seconds)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("remainingTime", remainingTime)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        remainingTime = savedInstanceState.getInt("remainingTime")
        updateTimerText()
        if (remainingTime <= 0) {
            textViewResendCode.isEnabled = true
        }
    }

    fun onBackArrowClicked(view: View?) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }






}
