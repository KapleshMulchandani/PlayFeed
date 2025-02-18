package com.example.playfeed

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import java.net.URL

class RssParser {

    fun parseRssFeed(rssFeed: String): List<RssArticle> {
        val articles = mutableListOf<RssArticle>()

        // Parse the RSS feed string
        val feed = SyndFeedInput().build(XmlReader(URL(rssFeed)))

        // Iterate over each RSS entry
        for (entry in feed.entries) {
            val title = entry.title
            val link = entry.link
            val description = entry.description?.value

            // Ensure we extract timestamp correctly as Long
            val timestamp = entry.updatedDate?.time ?: System.currentTimeMillis() // Fallback to current time in milliseconds

            val imageUrl = extractImage(description) // Extract the image URL from description if available
            val mediaUrl = extractMediaUrl(description) // Extract the media URL if available

            // Create an RssArticle with the required fields including timestamp
            val rssArticle = RssArticle(
                title = title,
                link = link,
                imageUrl = imageUrl ?: "https://community.cloudflare.steamstatic.com/public/shared/images/responsive/header_logo.png", // Use fallback image if imageUrl is null
                mediaUrl = mediaUrl,
                timestamp = timestamp
            )

            articles.add(rssArticle)
        }

        return articles
    }

    private fun extractImage(description: String?): String? {
        return try {
            Jsoup.parse(description).select("img").attr("src").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractMediaUrl(description: String?): String? {
        return try {
            Jsoup.parse(description).select("media\\:content").attr("url").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }
}
