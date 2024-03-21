package com.example.muhaddis_i210391


data class CommunityMessage(
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
