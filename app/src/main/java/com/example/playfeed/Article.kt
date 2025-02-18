package com.example.playfeed

sealed class Article {
    data class RssArticle(val title: String, val link: String, val imageUrl: String?) : Article()
    data class SteamArticle(val title: String, val url: String) : Article()
}
