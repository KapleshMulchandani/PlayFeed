package com.example.playfeed

import java.text.SimpleDateFormat
import java.util.*

data class RssArticle(
    val title: String,
    val link: String,
    val pubDate: String?, // Keep it as String
    val imageUrl: String?
) {
    // Convert the pubDate string to Date object
    fun getDate(): Date? {
        return try {
            pubDate?.let {
                val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
                format.parse(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
