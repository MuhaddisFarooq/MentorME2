package com.example.muhaddis_i210391

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.ChannelMediaOptions


class PhoneActivity : AppCompatActivity() {
    private val TAG = "PhoneActivity"
    private val PERMISSION_REQ_ID = 22
    private var mRtcEngine: RtcEngine? = null
    private lateinit var tvName: TextView
    private lateinit var ivMicMute: ImageView
    private lateinit var ivSpeaker: ImageView
    private lateinit var ivCross: ImageView

    private var isMuted = false

    private lateinit var tvTimer: TextView
    private var callDurationInSeconds = 0
    private val timerHandler = Handler(Looper.getMainLooper())


    private var isPaused = false // Flag to track pause state

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                // Increment call duration every second
                callDurationInSeconds++
                // Update the TextView
                tvTimer.text = formatDuration(callDurationInSeconds)
                // Post the task again with a delay of 1000 milliseconds
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i(TAG, "Join channel success, channel: $channel, uid: $uid")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "User offline, uid: $uid")
            // Here, you can update your UI or handle user offline events
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phonecall)
        tvTimer = findViewById(R.id.tvTimer)

        val callerName = intent.getStringExtra("CALLER_NAME") ?: "Unknown"

        // Set the retrieved name to the TextView
        tvName = findViewById(R.id.tvName)
        tvName.text = callerName

        val ivPause: ImageView = findViewById(R.id.ivPause)
        ivPause.setOnClickListener {
            // Toggle pause state
            isPaused = !isPaused
            if (!isPaused) {
                // If resuming, immediately run the timerRunnable to update UI and continue timing
                timerHandler.post(timerRunnable)
            }
            // Optionally, update the pause button's icon based on the new state
            ivPause.setImageResource(if (isPaused) R.drawable.play else R.drawable.pause)
        }

        if (checkAndRequestPermissions()) {
            initializeAgoraEngine()
        }

        setupUI()

        timerHandler.postDelayed(timerRunnable, 1000)


    }

    private fun setupUI() {
        ivMicMute = findViewById(R.id.ivMicMute)
        ivSpeaker = findViewById(R.id.ivSpeaker)
        ivCross = findViewById(R.id.ivCross)

        ivMicMute.setOnClickListener {
            isMuted = !isMuted
            mRtcEngine?.muteLocalAudioStream(isMuted)
            ivMicMute.setImageResource(if (isMuted) R.drawable.mute_mic else R.drawable.microphone)
        }

        ivSpeaker.setOnClickListener {
            // Handle speaker toggle
        }

        ivCross.setOnClickListener {
            finish()
        }
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, "0877bbbce9594c8f860c26194a00e3b2", mRtcEventHandler)
            mRtcEngine?.let {
                it.enableAudio()
                val options = ChannelMediaOptions().apply {
                    autoSubscribeAudio = true
                    autoSubscribeVideo = false
                }
                it.joinChannel(null, "yourChannelName", null, 0, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error message: ${e.message}")
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                PERMISSION_REQ_ID)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeAgoraEngine()
            } else {
                Log.i(TAG, "Permissions not granted by the user.")
                finish() // Permissions not granted, close the app.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
        timerHandler.removeCallbacks(timerRunnable)

    }

    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

}
