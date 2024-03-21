package com.example.muhaddis_i210391

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID




private const val REQUEST_MEDIA_PERMISSION_CODE = 1006


class IndividualChatActivity : AppCompatActivity() {



    companion object {
        private const val TAG = "IndividualChatActivity"
        private const val IMAGE_PICK_CODE = 1001
        private const val VIDEO_PICK_CODE = 1002
        private const val FILE_PICK_CODE = 1003
        private const val CAMERA_REQUEST_CODE = 1004
        private const val CAMERA_PERMISSION_CODE = 1005
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val REQUEST_MEDIA_PERMISSION_CODE = 1006
        private lateinit var currentPhotoPath: String
        private const val REQUEST_READ_STORAGE_PERMISSION = 1007

    }



    private lateinit var chatMessagesRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var inputField: EditText
    private lateinit var chatRoomId: String
    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var audioFileName: String? = null
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var receiverId: String // Define receiverId here
    private lateinit var rtcEngine: RtcEngine
    private val rtcEventHandler = object : IRtcEngineEventHandler() {
    }
    private val REQUEST_READ_MEDIA_IMAGES_PERMISSION = 1007 // Define a request code





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_chat)




        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_READ_MEDIA_IMAGES_PERMISSION)
        } else {
            registerScreenshotObserver()
        }

        try {
            rtcEngine = RtcEngine.create(baseContext, "0877bbbce9594c8f860c26194a00e3b2", rtcEventHandler)
            rtcEngine.enableAudio()
            rtcEngine.joinChannel(null, "channelName", null, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + Log.getStackTraceString(e))
        }

        receiverId = intent.getStringExtra("RECEIVER_ID") ?: "" // Initialize receiverId
        if (receiverId.isEmpty()) {
            finish() // Finish activity if receiverId is not found
            return
        }
        val userName = intent.getStringExtra("USER_NAME") ?: "Chat"

        findViewById<TextView>(R.id.tvChatName).text = userName


        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        if (senderId == null) {
            finish() // User must be logged in.
            return
        }
        chatRoomId = generateChatRoomId(senderId, receiverId)
        Log.d("ChatDebug", "ChatRoomId: $chatRoomId")

        chatMessagesRecyclerView = findViewById(R.id.rvChatMessages)
        chatAdapter = ChatAdapter(this, mutableListOf()) { chatMessage, action ->
            when (action) {
                "Edit" -> showEditDialog(chatMessage)
                "Delete" -> confirmDelete(chatMessage)
            }
        }
        chatMessagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IndividualChatActivity)
            adapter = chatAdapter
        }



        inputField = findViewById(R.id.etChatInput)
        findViewById<ImageView>(R.id.btnSend).setOnClickListener {
            val messageText = inputField.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, receiverId, senderId, chatRoomId,MessageType.SENT)
                inputField.setText("")
            }
        }

        listenForMessages(chatRoomId)


        val backArrow = findViewById<ImageView>(R.id.ivBack)
        backArrow.setOnClickListener() {
            finish()
        }

        val btnCall: ImageView = findViewById(R.id.ivCall)
        btnCall.setOnClickListener {
            val intent = Intent(this, PhoneActivity::class.java)
            intent.putExtra("CALLER_NAME", userName) // userName holds the name
            startActivity(intent)
        }




        val btnVideo = findViewById<ImageView>(R.id.ivVideo)
        btnVideo.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            startActivity(intent)
        }

        val btnCamera = findViewById<ImageView>(R.id.ivCamera)
        btnCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }



        findViewById<ImageView>(R.id.ivMic).setOnClickListener {
            if (checkPermissions()) {
                toggleRecording()
            } else {
                requestPermissions()
            }
        }

        // Add this inside your onCreate after initializing your components
        findViewById<ImageView>(R.id.ivAttachment).setOnClickListener {
            // Show options to choose from - Image, Video, or File
            showAttachmentOptions()
        }

        findViewById<ImageView>(R.id.ivCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
            } else {
                openCamera()
            }
        }





        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.selectedItemId = R.id.navigation_chat

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToMenu()
                    true
                }

                R.id.navigation_search -> {
                    val searchIntent = Intent(this, SearchActivity::class.java)
                    startActivity(searchIntent)
                    true
                }

                R.id.navigation_add -> {
                    val intent = Intent(this, AddMentorActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_chat -> {
                    val intent = Intent(this, ChatsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }


        }


    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Image", "Video", "File")
        AlertDialog.Builder(this)
            .setTitle("Select Attachment Type")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> pickImage()
                    1 -> pickVideo()
                    2 -> pickFile()
                }
            }.show()
    }


    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkMediaPermissions(): Boolean {
        val readImagesPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        val readVideoPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
        val readAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)

        return readImagesPermission == PackageManager.PERMISSION_GRANTED &&
                readVideoPermission == PackageManager.PERMISSION_GRANTED &&
                readAudioPermission == PackageManager.PERMISSION_GRANTED
    }

    // Request permissions if not granted
    private fun requestMediaPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ),
            REQUEST_MEDIA_PERMISSION_CODE
        )
    }

    private fun sendMessage(
        messageContent: String,
        receiverId: String,
        senderId: String,
        chatRoomId: String,
        messageType: MessageType
    ) {
        val message = ChatMessage(
            id = FirebaseDatabase.getInstance().reference.push().key ?: "",
            senderId = senderId,
            receiverId = receiverId,
            message = messageContent,
            timestamp = System.currentTimeMillis(),
            messageType = messageType
        )
        FirebaseDatabase.getInstance().getReference("chats/$chatRoomId").push().setValue(message)
    }

    private fun sendVoiceNote(
        voiceNoteUrl: String, // URL of the uploaded voice note
        receiverId: String,
        senderId: String,
        chatRoomId: String
    ) {
        val message = ChatMessage(
            id = FirebaseDatabase.getInstance().reference.push().key ?: "",
            senderId = senderId,
            receiverId = receiverId,
            message = voiceNoteUrl, // URL to the voice note in Firebase Storage
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.VOICE_NOTE // Set as VOICE_NOTE
        )

        FirebaseDatabase.getInstance().getReference("chats/$chatRoomId").push().setValue(message)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Voice note sent successfully")
            }
            .addOnFailureListener {
                Log.e("ChatActivity", "Failed to send voice note", it)
            }
    }


    private fun listenForMessages(chatRoomId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("chats/$chatRoomId")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatDebug", "DataSnapshot: $snapshot")
                val messages = mutableListOf<ChatMessage>()
                snapshot.children.forEach { child ->
                    Log.d("ChatDebug", "Child: ${child.key} => ${child.value}")
                    val id = child.key ?: "" // Handle nullability
                    val senderId = child.child("senderId").getValue(String::class.java)
                    val receiverId = child.child("receiverId").getValue(String::class.java)
                    val message = child.child("message").getValue(String::class.java)
                    val timestamp = child.child("timestamp").getValue(Long::class.java)
                    val messageType = child.child("messageType").getValue(MessageType::class.java)

                    // Log the retrieved message content
                    Log.d(
                        "ChatDebug",
                        "Message: $id, $senderId, $receiverId, $message, $timestamp, $messageType"
                    )

                    val chatMessage =
                        ChatMessage(id, senderId, receiverId, message, timestamp, messageType)
                    messages.add(chatMessage)
                }
                chatAdapter.updateMessages(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatDebug", "listenForMessages:onCancelled: ${error.toException()}")
                // Consider displaying a message or handling the error as needed.
            }
        })


    }



    private fun generateChatRoomId(senderId: String, receiverId: String): String {
        return if (senderId > receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
    }


    private fun showEditDialog(chatMessage: ChatMessage) {
        val editText = EditText(this).apply { setText(chatMessage.message) }
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val editedText = editText.text.toString()
                updateMessageInFirebase(
                    chatMessage,
                    editedText
                ) // Pass the entire chatMessage object
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(chatMessage: ChatMessage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessageFromFirebase(chatMessage) // Pass the entire chatMessage object
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateMessageInFirebase(chatMessage: ChatMessage, newText: String) {
        val messageRef =
            FirebaseDatabase.getInstance().getReference("chats/$chatRoomId/${chatMessage.id}")
        messageRef.child("message").setValue(newText).addOnSuccessListener {
            // Refresh the messages list
            listenForMessages(chatRoomId)
        }.addOnFailureListener {
            Log.e("ChatActivity", "Failed to update message in Firebase.", it)
        }
    }

    private fun deleteMessageFromFirebase(chatMessage: ChatMessage) {
        val messageRef =
            FirebaseDatabase.getInstance().getReference("chats/$chatRoomId/${chatMessage.id}")
        messageRef.removeValue().addOnSuccessListener {
            // Refresh the messages list
            listenForMessages(chatRoomId)
        }.addOnFailureListener {
            Log.e("ChatActivity", "Failed to delete message from Firebase.", it)
        }
    }


    private fun checkPermissions(): Boolean {
        val recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return recordPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    toggleRecording()
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
                }
            }



            REQUEST_READ_MEDIA_IMAGES_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerScreenshotObserver()
                } else {
                    Toast.makeText(
                        this,
                        "Permission to read media images is denied.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }




    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val outputFile = getOutputFile()
        audioFileName = outputFile.absolutePath
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                Log.e("AudioRecord", "prepare() failed")
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            mediaRecorder = null
            isRecording = false
            audioFileName?.let { fileName ->
                // Assuming uploadAudioToFirebaseStorage now returns the download URL
                uploadAudioToFirebaseStorage(fileName) { downloadUrl ->
                    // Call sendVoiceNote with the URL once the file is uploaded
                    sendVoiceNote(downloadUrl, receiverId, FirebaseAuth.getInstance().currentUser?.uid ?: "", chatRoomId)
                }
            }
        }
    }

    private fun getOutputFile(): File {
        // Use getExternalFilesDir for external storage or getFilesDir for internal storage
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File(storageDir, "${System.currentTimeMillis()}.mp3")
    }


    private fun uploadAudioToFirebaseStorage(fileName: String, callback: (String) -> Unit) {
        val fileUri = Uri.fromFile(File(fileName))
        val storageRef = FirebaseStorage.getInstance().reference.child("voiceNotes/${fileUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()
                callback(downloadUrl)
            } else {
                // Handle failures
            }
        }
    }





    // Function to pick an image
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Function to pick a video
    private fun pickVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, VIDEO_PICK_CODE)
    }

    // Function to pick a file
    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val fileUri = Uri.fromFile(File(currentPhotoPath))
                    uploadFile(fileUri, MessageType.IMAGE)
                }
                IMAGE_PICK_CODE -> {
                    data?.data?.let { uploadFile(it, MessageType.IMAGE) }
                }
                VIDEO_PICK_CODE -> {
                    data?.data?.let { uploadFile(it, MessageType.VIDEO) }
                }
                FILE_PICK_CODE -> {
                    data?.data?.let { uploadFile(it, MessageType.FILE) }
                }
            }
        }
    }


    private fun uploadFile(fileUri: Uri, messageType: MessageType) {
        val fileRef = FirebaseStorage.getInstance().reference.child("chat_files/${UUID.randomUUID()}")
        fileRef.putFile(fileUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                sendMessage(downloadUri, receiverId, FirebaseAuth.getInstance().currentUser?.uid ?: "", chatRoomId, messageType)
            } else {
                Toast.makeText(this, "File upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }






    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.e(TAG, "Cannot create a file for the image", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.muhaddis_i210391.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: throw IOException("Unable to access storage directory.")
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()

    }
    private fun registerScreenshotObserver() {
        val contentResolver = contentResolver
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.registerContentObserver(externalUri, true, object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                queryForScreenshot(uri)
            }
        })
    }

    private fun queryForScreenshot(uri: Uri?) {
        uri ?: return

        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val data = it.getString(dataIndex)

                if (name.contains("screenshot", ignoreCase = true) || data.contains("screenshots", ignoreCase = true)) {
                    notifyScreenshotTaken()
                    break
                }
            }
        }
    }

    private fun notifyScreenshotTaken() {
        // Example of showing a Toast notification
        Toast.makeText(this, "Screenshot taken!", Toast.LENGTH_SHORT).show()

        // Alternatively, you can create a notification
        createScreenshotNotification()
    }

    private fun createScreenshotNotification() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("screenshot_channel", "Screenshots")
        } else {
            // If earlier version channel ID is not used
            ""
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.mipmap.notification)
            setContentTitle("Screenshot Detected")
            setContentText("A screenshot was taken of the chat.")
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}