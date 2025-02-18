package com.example.playfeed

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playfeed.R
import androidx.lifecycle.Observer

class GameDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel
    private val steamNewsFetcher = SteamNewsFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        // Get game name and image from the intent
        val gameName = intent.getStringExtra("GAME_NAME") ?: "Game"
        val gameImageRes = intent.getIntExtra("GAME_IMAGE", R.drawable.ic_launcher_foreground)

        // Set up UI
        findViewById<ImageView>(R.id.gameImage).setImageResource(gameImageRes)
        findViewById<TextView>(R.id.gameName).text = gameName

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter

        // Initialize ViewModel
        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)

        // Fetch and display news for the game
        fetchRssNews(gameName)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun fetchRssNews(gameName: String) {
        val rssUrl = when (gameName.lowercase()) {
            "cs2" -> "https://www.hltv.org/rss/news"
            "valorant" -> "https://vlr.gg/rss/news"
            "league of legends" -> "https://www.leagueoflegends.com/en-us/news/rss/"
            "dota 2" -> "https://www.dota2.com/news/rss"
            "rainbow six siege" -> "https://www.ubisoft.com/rss/game-news"
            else -> {
                Toast.makeText(this, "No news source found for $gameName", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Check if we are fetching for Steam or HLTV
        if (gameName.lowercase() == "valorant" || gameName.lowercase() == "cs2") {
            fetchSteamNews(gameName) // Fetch steam related news for CS2/Valorant
        } else {
            rssViewModel.fetchRss(rssUrl) // Use ViewModel for other games
        }

        // Observe the articles LiveData for updates
        rssViewModel.articles.observe(this, Observer { articles ->
            // Wrap the articles in the sealed class Article
            val wrappedArticles = articles.map { Article.RssArticle(it.title, it.link, it.imageUrl) }
            // Update the adapter with the latest list of articles
            articleAdapter.updateArticles(wrappedArticles)
        })
    }


    private fun fetchSteamNews(gameName: String) {
        val appId = when (gameName.lowercase()) {
            "cs2" -> 730 // CS:GO's AppID for CS2
            "valorant" -> 0 // This can be updated when we have a valid appId
            else -> {
                // Show an error toast if the game is unrecognized
                Toast.makeText(this, "Unknown game selected", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val steamNewsFetcher = SteamNewsFetcher()

        steamNewsFetcher.fetchSteamNews(appId,
            onSuccess = { articles ->
                // Wrap Steam articles into Article.SteamArticle
                val wrappedArticles = articles.map { Article.SteamArticle(it.title, it.url) }

                // Ensure the UI update happens on the main thread
                runOnUiThread {
                    // Update the adapter with the list of Steam articles
                    articleAdapter.updateArticles(wrappedArticles)
                }
            },
            onFailure = { errorMessage ->
                Log.e("SteamNewsFetcher", "Failed to fetch news: $errorMessage")

                // Show an error toast on the main thread
                runOnUiThread {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


}
