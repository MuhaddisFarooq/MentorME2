package com.example.muhaddis_i210391

data class Mentorr(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val imageUrl: String = "",
    var isFavorite: Boolean = false

) {
    fun toggleFavorite() {
        isFavorite = !isFavorite
    }
}
