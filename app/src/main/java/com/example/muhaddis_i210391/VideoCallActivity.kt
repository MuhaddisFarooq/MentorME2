package com.example.muhaddis_i210391

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class VideoCallActivity : AppCompatActivity() {
    private var mRtcEngine: RtcEngine? = null
    private val TAG = "VideoCallActivity"

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { removeRemoteVideo() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.videocall)

        if (!hasPermission(Manifest.permission.RECORD_AUDIO) || !hasPermission(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), PERMISSION_REQ_ID)
        } else {
            // Initialize and join the channel if permissions are already granted
            initializeAndJoinChannel()
        }

        findViewById<ImageView>(R.id.Cross).setOnClickListener {
            leaveChannel()
            finish()
        }

        findViewById<ImageView>(R.id.rotatebutton).setOnClickListener {
            switchCamera()
        }
    }


    private fun switchCamera() {
        mRtcEngine?.switchCamera()
    }
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, "0877bbbce9594c8f860c26194a00e3b2", mRtcEventHandler)
            mRtcEngine?.enableVideo()

            val localView = RtcEngine.CreateRendererView(baseContext)
            mRtcEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            findViewById<FrameLayout>(R.id.videoPreviewPlaceholder).addView(localView)

            mRtcEngine?.setVideoEncoderConfiguration(VideoEncoderConfiguration())
            mRtcEngine?.joinChannel(null, "yourChannelName", "Extra Optional Data", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Agora RtcEngine: ${e.localizedMessage}")
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val remoteView = RtcEngine.CreateRendererView(baseContext)
        mRtcEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
        findViewById<FrameLayout>(R.id.videoPreviewPlaceholder).addView(remoteView)
    }

    private fun removeRemoteVideo() {
        findViewById<FrameLayout>(R.id.videoPreviewPlaceholder).removeAllViews()
    }

    private fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.let {
            RtcEngine.destroy()
            mRtcEngine = null
        }
    }

    companion object {
        const val PERMISSION_REQ_ID = 22
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeAndJoinChannel()
            } else {
                Log.i(TAG, "Permissions not granted")
            }
        }
    }
}
