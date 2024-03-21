package com.example.muhaddis_i210391
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.File

data class ChatMessage(
    var id: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: Long? = null,
    val messageType: MessageType? = null
)
 {
     constructor() : this(null, null, null, null, null, null)
}


enum class MessageType {
    SENT, RECEIVED, VOICE_NOTE, IMAGE, VIDEO, SYSTEM,FILE;



    // If you need a string representation in Firebase, you might need a property or function here:
    override fun toString(): String {
        return this.name // Returns the name of the enum constant, exactly as declared in its enum declaration
    }
}
private var mediaPlayer: MediaPlayer? = null

class ChatAdapter(
    private val context: Context,
    private var chatMessages: MutableList<ChatMessage>,
    private val onMessageInteraction: (ChatMessage, String) -> Unit
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2

    }

    override fun getItemViewType(position: Int): Int {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (chatMessages[position].senderId == currentUserId) VIEW_TYPE_MESSAGE_SENT else VIEW_TYPE_MESSAGE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId =
            if (viewType == VIEW_TYPE_MESSAGE_SENT) R.layout.item_chat_message_sent else R.layout.item_chat_message_received
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    fun updateMessages(messages: List<ChatMessage>) {
        this.chatMessages = messages.toMutableList()
        notifyDataSetChanged()
    }

    fun addMessage(chatMessage: ChatMessage) {
        chatMessages.add(chatMessage)
        notifyItemInserted(chatMessages.size - 1)
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.bind(chatMessage, onMessageInteraction)
    }

    override fun getItemCount() = chatMessages.size

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewSent: TextView? = view.findViewById(R.id.text_message_body_sent)
        private val textViewReceived: TextView? = view.findViewById(R.id.text_message_body_received)
        private val playVoiceNoteButton: ImageView? = view.findViewById(R.id.play_voice_note_button)

        private val imageView: ImageView = view.findViewById(R.id.image_message_sent)
        private val videoThumbnailView: ImageView = view.findViewById(R.id.video_thumbnail_sent)
        private val fileNameView: TextView = view.findViewById(R.id.file_name_sent)

        fun bind(chatMessage: ChatMessage, onMessageInteraction: (ChatMessage, String) -> Unit) {
            textViewSent?.visibility = View.GONE
            textViewReceived?.visibility = View.GONE
            playVoiceNoteButton?.visibility = View.GONE
            imageView.visibility = View.GONE
            videoThumbnailView.visibility = View.GONE
            fileNameView.visibility = View.GONE



            when (chatMessage.messageType) {
                MessageType.SENT, MessageType.RECEIVED -> {
                    textViewSent?.text = chatMessage.message
                    textViewSent?.visibility =
                        if (chatMessage.messageType == MessageType.SENT) View.VISIBLE else View.GONE
                    textViewReceived?.visibility =
                        if (chatMessage.messageType == MessageType.RECEIVED) View.VISIBLE else View.GONE
                    playVoiceNoteButton?.visibility = View.GONE
                }

                MessageType.VOICE_NOTE -> {
                    playVoiceNoteButton?.visibility = View.VISIBLE
                    playVoiceNoteButton?.setOnClickListener {
                        playVoiceNote(chatMessage.message ?: "")
                    }
                }

                MessageType.IMAGE -> {
                    imageView.visibility = View.VISIBLE
                    chatMessage.message?.let { imageUrl ->
                        // Load the image with Picasso
                        Picasso.get()
                            .load(imageUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE) // Try to load from cache; if unavailable, go online
                            .into(imageView, object : Callback {
                                override fun onSuccess() {
                                }

                                override fun onError(e: Exception?) {
                                    Picasso.get().load(imageUrl).into(imageView)
                                }
                            })
                    }
                }

                MessageType.VIDEO -> {
                    videoThumbnailView.visibility = View.VISIBLE
                    // Set a placeholder or static image as the thumbnail
                    Glide.with(context).load(R.drawable.video_thumbnail).into(videoThumbnailView)
                    // Optionally, if you have a way to retrieve or generate thumbnails, use that URL instead of a static resource
                }
                MessageType.FILE -> {
                    // Show file icon and file name. Adjust this part as needed
                    fileNameView.visibility = View.VISIBLE
                    fileNameView.text = Uri.parse(chatMessage.message).lastPathSegment // Example to get file name
                    // Assuming you're setting a generic file icon for all files, adjust if you have specific icons for each type
                }

                MessageType.SYSTEM -> {
                    textViewSent?.apply {
                        text = chatMessage.message
                        visibility = View.VISIBLE
                    }
                }


                else -> {
                    // Handle null or any other unexpected type
                    Log.e(
                        "ChatAdapter",
                        "Unexpected MessageType or null: ${chatMessage.messageType}"
                    )
                }


            }


            itemView.setOnLongClickListener {
                val options = arrayOf("Edit", "Delete")
                MaterialAlertDialogBuilder(context)
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> onMessageInteraction(chatMessage, "Edit")
                            1 -> onMessageInteraction(chatMessage, "Delete")
                        }
                    }.show()
                true
            }


        }
    }

    // Updated method to play voice note from Firebase Storage
    private fun playVoiceNote(downloadUrl: String) {
        if (downloadUrl.isEmpty()) {
            Toast.makeText(context, "Voice note URL is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a reference to the voice note using its URL
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl)

        // Create a temporary file in the app's external files directory
        val tempFile = File.createTempFile(
            "voiceNote",
            "mp3",
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        )

        storageRef.getFile(tempFile).addOnSuccessListener {
            // Successfully downloaded the file, attempt to play it
            mediaPlayer?.release()  // Release any previously playing media player
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(tempFile.absolutePath)
                prepare()  // Prepare the MediaPlayer asynchronously to not block the main thread
                start()  // Start playback
                setOnCompletionListener { mp ->
                    mp.release()  // Release the MediaPlayer when playback is complete
                    mediaPlayer = null
                }
            }
        }.addOnFailureListener { exception ->
            // Handle any errors during the download
            Log.e("ChatAdapter", "Failed to download voice note: $downloadUrl", exception)
            Toast.makeText(
                context,
                "Failed to play voice note: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


}