package com.example.playfeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class RssViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<RssArticle>>()
    val articles: LiveData<List<RssArticle>> = _articles

    // Fetch RSS feed from a given URL
    fun fetchRss(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create a URL object for the RSS feed
                val feedUrl = URL(url)
                val inputStream = feedUrl.openStream()

                // Use the RSS parser to parse the feed
                val rssParser = RssParser()
                val fetchedArticles = rssParser.parse(inputStream)

                // Post the parsed articles back to the main thread
                _articles.postValue(fetchedArticles)
            } catch (e: Exception) {
                e.printStackTrace()
                // In case of an error, post an empty list to the LiveData
                _articles.postValue(emptyList())
            }
        }
    }
}
