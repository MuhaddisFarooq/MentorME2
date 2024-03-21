package com.example.muhaddis_i210391

data class Review(
    val reviewText: String = "",
    val rating: Float = 0f,
    val userId: String = "",
    val mentorName: String? = null, // Display mentor name alongside review
    val mentorId: String? = null // Keep track of which mentor the review is for
)
