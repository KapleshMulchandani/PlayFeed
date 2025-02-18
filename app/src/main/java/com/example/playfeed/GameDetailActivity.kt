package com.example.playfeed

import android.os.Bundle
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

        // Fetch and display RSS news for the game
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

        // Fetch RSS feed and observe changes
        rssViewModel.fetchRss(rssUrl)

        // Observe the articles LiveData for updates
        rssViewModel.articles.observe(this, Observer { articles ->
            // Update the adapter with the latest list of articles
            if (articles != null) {
                articleAdapter.updateArticles(articles)
            }
        })
    }
}