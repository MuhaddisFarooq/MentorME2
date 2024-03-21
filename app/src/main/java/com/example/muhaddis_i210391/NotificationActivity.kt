package com.example.muhaddis_i210391

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationsActivity : AppCompatActivity() {

    private lateinit var databaseNotifications: DatabaseReference
    private lateinit var notificationList: MutableList<Notification>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        fetchNotifications()


        val notificationsListView = findViewById<ListView>(R.id.lvNotifications)
        notificationList = mutableListOf()

        databaseNotifications = FirebaseDatabase.getInstance().getReference("notifications")

        val ivBackArrow = findViewById<ImageView>(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener { finish() }

        val tvClearAll = findViewById<TextView>(R.id.tvClearAll)
        tvClearAll.setOnClickListener {
        }

        // Listen for database changes
        databaseNotifications.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                for (postSnapshot in snapshot.children) {
                    val notification = postSnapshot.getValue(Notification::class.java)
                    notification?.id = postSnapshot.key ?: ""
                    notification?.let { notificationList.add(it) }
                }
                val adapter = ArrayAdapter(
                    this@NotificationsActivity,
                    android.R.layout.simple_list_item_1,
                    notificationList.map { it.title + ": " + it.message })
                notificationsListView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log error
            }
        })


    }

    private fun fetchNotifications() {
        FirebaseDatabase.getInstance().getReference("notifications")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val notifications = mutableListOf<String>()
                    dataSnapshot.children.forEach { snapshot ->
                        val message = snapshot.child("message").getValue(String::class.java)
                        message?.let { notifications.add(it) }
                    }
                    updateListView(notifications)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("NotificationsActivity", "Error fetching notifications", databaseError.toException())
                }
            })
    }

    private fun updateListView(notifications: List<String>) {
        // Assuming you have a ListView and an Adapter set up similarly to how you did before
        val adapter = SessionAdapter2(this, R.layout.list_item_notification, notifications)
        findViewById<ListView>(R.id.lvNotifications).adapter = adapter
    }




}
