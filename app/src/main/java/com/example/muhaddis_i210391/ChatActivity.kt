package com.example.muhaddis_i210391

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView



class ChatsActivity : AppCompatActivity(), MessageAdapter.OnItemClickListener {


    private val communityList = listOf(
        CommunityItem(id = 1, name = "John Doe", profileImage = R.mipmap.profile_john),
        CommunityItem(id = 2, name = "Jack Watson", profileImage = R.mipmap.profile_lee),
        CommunityItem(id = 3, name = "Jen Lee", profileImage = R.mipmap.profile_jen),
        CommunityItem(id = 4, name = "Kelly", profileImage = R.mipmap.profile_kell),


        )

    private val messagesList = listOf(
        MessageItem(id = 1, name = "John Cooper", messagePreview = "Hey, how are you?", imageResId = R.mipmap.profile_john, newMessageCount = 1, receiverId = "receiverId1"),
        MessageItem(id = 2, name = "Jack Watson", messagePreview = "Hey, Man", imageResId = R.mipmap.profile_lee, newMessageCount = 1, receiverId = "receiverId2"),
        MessageItem(id = 3, name = "Kelly Shelby", messagePreview = "Hi, ", imageResId = R.mipmap.profile_kell, newMessageCount = 1, receiverId = "receiverId3")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)




        // Set up the community icons RecyclerView
        val communityRecyclerView: RecyclerView = findViewById(R.id.rvCommunityIcons)
        communityRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        communityRecyclerView.adapter = CommunityAdapter(communityList) { communityItem ->
            // When a community item is clicked, navigate to CommunityActivity
            val intent = Intent(this, CommunityActivity::class.java)
            intent.putExtra("COMMUNITY_ID", communityItem.id)
            startActivity(intent)
        }
        communityRecyclerView.setHasFixedSize(true)

        val messagesRecyclerView: RecyclerView = findViewById(R.id.rvChatItems)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = MessageAdapter(messagesList, this) // Correctly passing 'this' as the listener
        messagesRecyclerView.setHasFixedSize(true)


        findViewById<ImageView>(R.id.ivBackArrow).setOnClickListener {
            // Navigate back to MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
        // BottomNavigationView setup
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
                    val searchIntent = Intent(this, AddMentorActivity::class.java)
                    startActivity(searchIntent)
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, MyProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false             }
        }



    }

    override fun onItemClick(messageItem: MessageItem) {
        val intent = Intent(this, IndividualChatActivity::class.java).apply {
            putExtra("RECEIVER_ID", messageItem.receiverId)
            putExtra("USER_NAME", messageItem.name) // Pass the user name
        }
        startActivity(intent)
    }



    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }



}

