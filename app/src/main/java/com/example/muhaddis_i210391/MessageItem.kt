package com.example.muhaddis_i210391

data class MessageItem(
    val id: Int,
    val name: String,
    val messagePreview: String,
    val imageResId: Int,
    val newMessageCount: Int,
    val receiverId: String
)