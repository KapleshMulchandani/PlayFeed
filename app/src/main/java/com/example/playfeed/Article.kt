package com.example.playfeed

// Create an interface or superclass for articles
sealed class Article {
    data class RssArticle(
        val title: String,
        val link: String,
        val imageUrl: String?,
        val mediaUrl: String?,
        val timestamp: Long,
        val source: String = "RSS"
    ) : Article()

    data class SteamArticle(
        val title: String,
        val link: String,
        val imageUrl: String?,
        val mediaUrl: String?,
        val timestamp: Long,
        val source: String = "Steam"
    ) : Article()
}
