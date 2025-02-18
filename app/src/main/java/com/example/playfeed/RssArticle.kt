package com.example.playfeed

data class RssArticle(
    val title: String,
    val link: String,
    val imageUrl: String?,
    val mediaUrl: String?,
    val timestamp: Long,
    val source: String = "RSS"
)