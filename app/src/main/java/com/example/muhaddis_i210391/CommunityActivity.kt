package com.example.muhaddis_i210391

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommunityActivity : AppCompatActivity() {
    private lateinit var communityMessagesRecyclerView: RecyclerView
    private lateinit var communityChatAdapter: CommunityMessageAdapter
    private lateinit var communityLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val communityMessages = listOf(
            CommunityMessage(senderName = "Alice", message = "Hello, everyone!"),
            CommunityMessage(senderName = "Bob", message = "How's it going?")

        )

        communityMessagesRecyclerView = findViewById(R.id.rvCommunityMessages)
        communityLayoutManager = LinearLayoutManager(this)
        communityMessagesRecyclerView.layoutManager = communityLayoutManager

        communityChatAdapter = CommunityMessageAdapter(communityMessages)
        communityMessagesRecyclerView.adapter = communityChatAdapter



        val btnCall= findViewById<ImageView>(R.id.ivCall)
        btnCall.setOnClickListener {
            val intent = Intent(this, PhoneActivity::class.java)
            startActivity(intent)
        }


        val btnVideo= findViewById<ImageView>(R.id.ivVideo)
        btnVideo.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            startActivity(intent)
        }

        val backArrow = findViewById<ImageView>(R.id.ivBack)
        backArrow.setOnClickListener {
            finish()
        }



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.selectedItemId = R.id.navigation_chat

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToMenu()
                    true
                }

                R.id.navigation_chat -> {
                    val intent = Intent(this, ChatsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_add -> {
                    val intent = Intent(this, AddMentorActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, MyProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false            }
        }

        listenForMessages()


        findViewById<ImageView>(R.id.btnSend).setOnClickListener {
            sendMessage()
        }



    }



    private fun sendMessage() {
        val inputField: EditText = findViewById(R.id.etChatInput)
        val messageText = inputField.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Log.e("CommunityActivity", "UID is null.")
                return
            }

            FirebaseDatabase.getInstance().getReference("/Users/$uid/name").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userName = snapshot.value.toString()
                    Log.d("CommunityActivity", "Fetched user name: $userName")

                    val message = CommunityMessage(senderName = userName, message = messageText)
                    FirebaseDatabase.getInstance().getReference("/communityMessages").push().setValue(message)
                    inputField.setText("")
                } else {
                    Log.d("CommunityActivity", "Snapshot does not exist or name is null")
                }
            }.addOnFailureListener { exception ->
                Log.e("CommunityActivity", "Error fetching user name", exception)
            }
        }
    }





    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }



    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/communityMessages")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<CommunityMessage>()
                snapshot.children.forEach { child ->
                    val message = child.getValue(CommunityMessage::class.java)
                    message?.let {
                        messages.add(it)
                    }
                }
                // Update the adapter with the new messages
                communityChatAdapter = CommunityMessageAdapter(messages)
                communityMessagesRecyclerView.adapter = communityChatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommunityActivity", "Failed to listen for messages", error.toException())
            }
        })
    }



}




