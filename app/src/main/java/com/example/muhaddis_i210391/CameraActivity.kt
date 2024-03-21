package com.example.muhaddis_i210391

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    private lateinit var captureButton: ImageView
    private lateinit var videoButton: ImageView
    private lateinit var galleryButton: ImageView
    private lateinit var closeCamera: ImageView
    private lateinit var hdrText: TextView
    private lateinit var settings: ImageView

    private lateinit var slowMoOption: TextView
    private lateinit var videoOption: TextView
    private lateinit var photoOption: TextView
    private lateinit var squareOption: TextView
    private lateinit var portraitOption: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)



        captureButton = findViewById(R.id.captureButton)
        videoButton = findViewById(R.id.videoButton)
        galleryButton = findViewById(R.id.gallery)
        closeCamera = findViewById(R.id.closeCamera)
        hdrText = findViewById(R.id.hdrText)
        settings = findViewById(R.id.settings)

        slowMoOption = findViewById(R.id.slowMo)
        videoOption = findViewById(R.id.video)
        photoOption = findViewById(R.id.photo)
        squareOption = findViewById(R.id.square)
        portraitOption = findViewById(R.id.portrait)

        setOptionClickListeners()


        closeCamera.setOnClickListener {
            finish()
        }
    }

    private fun setOptionClickListeners() {
        val optionClickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.slowMo -> selectOption(slowMoOption)
                R.id.video -> selectOption(videoOption)
                R.id.photo -> selectOption(photoOption)
                R.id.square -> selectOption(squareOption)
                R.id.portrait -> selectOption(portraitOption)
            }
        }

        slowMoOption.setOnClickListener(optionClickListener)
        videoOption.setOnClickListener(optionClickListener)
        photoOption.setOnClickListener(optionClickListener)
        squareOption.setOnClickListener(optionClickListener)
        portraitOption.setOnClickListener(optionClickListener)
    }

    private fun selectOption(selectedOption: TextView) {
        // Reset all options to default
        slowMoOption.isSelected = false
        videoOption.isSelected = false
        photoOption.isSelected = false
        squareOption.isSelected = false
        portraitOption.isSelected = false

        // Select the clicked one
        selectedOption.isSelected = true

    }
}
