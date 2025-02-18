package com.example.playfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import java.net.URL

class RssViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<RssArticle>>()
    val articles: LiveData<List<RssArticle>> = _articles

    fun fetchRss(url: String) {
        // Run the network task in the background (IO dispatcher)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch and parse the RSS feed
                val articles = mutableListOf<RssArticle>()
                val feedSource = URL(url)
                val input = SyndFeedInput().build(XmlReader(feedSource))

                // Parse each entry in the feed
                for (entry in input.entries) {
                    val title = entry.title
                    val link = entry.link
                    val imageUrl = extractImage(entry.description.value)

                    // Extract mediaUrl (if available)
                    val mediaUrl = extractMediaUrl(entry.description.value)

                    // Get the timestamp (convert to Long if available)
                    val timestamp = entry.publishedDate?.time ?: 0L // Convert to Long (milliseconds)

                    // Create RssArticle with timestamp
                    articles.add(RssArticle(title, link, imageUrl, mediaUrl, timestamp))
                }

                // Post the result back to the main thread
                _articles.postValue(articles)
            } catch (e: Exception) {
                e.printStackTrace()
                _articles.postValue(emptyList()) // Handle error by sending an empty list
            }
        }
    }

    // Helper function to extract image URL from the RSS entry's description
    private fun extractImage(description: String?): String? {
        return try {
            Jsoup.parse(description).select("img").attr("src").takeIf { it.isNotEmpty() }
                ?: "https://example.com/default_image.png" // Fallback URL for the image
        } catch (e: Exception) {
            null // Return null if no image found
        }
    }

    // Helper function to extract media URL from the RSS entry's description (if available)
    private fun extractMediaUrl(description: String?): String? {
        return try {
            // Assuming the media URL is embedded in the description as a <media:content> tag
            Jsoup.parse(description).select("media\\:content").attr("url")
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null // Return null if no media URL is found
        }
    }
}
