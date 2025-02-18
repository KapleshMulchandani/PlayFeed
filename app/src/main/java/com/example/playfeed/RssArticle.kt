package com.example.playfeed

data class RssArticle(
    val title: String,
    val link: String,
    val imageUrl: String?, // Optional image URL
    val mediaUrl: String?, // Optional media URL
    val timestamp: Long    // Timestamp in milliseconds
)