package com.example.playfeed

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import java.util.Date

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
        fetchNews(gameName)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun fetchNews(gameName: String) {
        val allArticles = mutableListOf<Any>()
        val feedUrls = getRssUrls(gameName)

        for (url in feedUrls) {
            rssViewModel.fetchRss(url)
        }

        rssViewModel.articles.observe(this, Observer { rssArticles ->
            rssArticles?.let {
                allArticles.addAll(it)
            }

            // Sort all articles by date (newest first)
            allArticles.sortByDescending { article ->
                when (article) {
                    is RssArticle -> article.getDate() ?: Date(0) // Default to epoch if null
                    else -> Date(0) // Fallback case
                }
            }


            // Update the adapter
            articleAdapter.updateArticles(allArticles)
        })
    }

    private fun getRssUrls(gameName: String): List<String> {
        return when (gameName.lowercase()) {
            "cs:go", "cs2" -> listOf(
                "https://www.dexerto.com/feed/category/counter-strike-2/",
                "https://dotesports.com/counter-strike/feed",
                "https://www.oneesports.gg/counter-strike-2/feed/",
                "https://esportsinsider.com/esports-titles/shooters/counter-strike/feed",
                "https://www.hltv.org/rss/news",
                "https://raw.githubusercontent.com/IceQ1337/CS-RSS-Feed/master/feeds/news-feed-en.xml",
                "https://raw.githubusercontent.com/IceQ1337/CS-RSS-Feed/master/feeds/updates-feed-en.xml",
            )
            "valorant" -> listOf(
                "https://dotesports.com/valorant/feed",
                "https://www.dexerto.com/valorant/feed/",
                "https://www.oneesports.gg/valorant/feed/",
                "https://data.rito.news/valoramt/en-us/news.rss",
            )
            "league of legends" -> listOf(
                "https://www.dexerto.com/league-of-legends/feed/",
                "https://dotesports.com/league-of-legends/feed",
                "https://www.oneesports.gg/league-of-legends/feed/",
                "https://data.rito.news/lol/en-us/news.rss",
                "https://data.rito.news/lol/en-us/esports.rss"
            )
            "dota 2" -> listOf(
                "https://dotesports.com/dota-2/feed",
                "https://www.oneesports.gg/dota2/feed/"
                )
            "fortnite" -> listOf(
                "https://www.dexerto.com/feed/category/fortnite/",
                "https://dotesports.com/fortnite/feed",
                "https://www.oneesports.gg/fortnite/feed/"
            )
            "other" -> listOf(
                "https://www.oneesports.gg/gaming/feed/",
                "https://dotesports.com/reviews/feed"
            )
            else -> emptyList() // No source if no match
        }
    }
}