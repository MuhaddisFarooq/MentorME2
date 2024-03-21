package com.example.muhaddis_i210391


import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class BookedSessionsActivity : AppCompatActivity() {

    private lateinit var lvBookedSessions: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_sessions)

        lvBookedSessions = findViewById(R.id.lvBookedSessions)

        val sessions = listOf(
            Session("John Cooper", "UX Desginer","24th Dec 2023 - 1:00 pm", R.mipmap.profile_john),
            Session("Emma Phillips", "Android Developer","1st Jan 2024 - 9:00 pm", R.mipmap.profile_kell)
        )

        val sessionsAdapter = SessionAdapter(this, R.layout.list_item_session, sessions)
        lvBookedSessions.adapter = sessionsAdapter

        findViewById<ImageView>(R.id.ivBackArrow).setOnClickListener {
            finish()
        }
    }
    data class Session(
        val name: String,
        val designation: String,
        val date: String,
        val profileImageResource: Int
    )
}
