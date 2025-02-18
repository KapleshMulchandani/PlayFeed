package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class HomeActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var userButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        homeButton = findViewById(R.id.homeButton)
        searchButton = findViewById(R.id.searchButton)
        userButton = findViewById(R.id.userButton)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter

        // Initialize ViewModel
        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)

        // Set up navigation
        homeButton.setOnClickListener {
            navigateToActivity(HomeActivity::class.java)
        }

        searchButton.setOnClickListener {
            navigateToActivity(CategoriesActivity::class.java)
        }

        userButton.setOnClickListener {
            navigateToActivity(UserActivity::class.java)
        }

        // Fetch followed games and their articles
        val userId = getLoggedInUserId()  // Replace with actual method to get logged-in user's ID
        fetchFollowedGames(userId)
    }

    // Helper function to get logged-in user's ID (replace with actual implementation)
    private fun getLoggedInUserId(): String {
        // Return the user's ID from your authentication system
        return "user123"  // Example placeholder
    }
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    // Fetch followed games from Firebase Firestore
    private fun fetchFollowedGames(userId: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val followedGames = document.get("followedGames") as? List<String> ?: emptyList()
                fetchArticlesForFollowedGames(followedGames)
            }
            .addOnFailureListener {
                // Handle failure (e.g., log error)
            }
    }

    // Fetch articles for the followed games
    private fun fetchArticlesForFollowedGames(followedGames: List<String>) {
        val allArticles = mutableListOf<Any>()

        // Fetch RSS and Steam articles for each followed game
        for (gameName in followedGames) {
            val feedUrls = getRssUrls(gameName)

            for (url in feedUrls) {
                rssViewModel.fetchRss(url)
            }

            rssViewModel.articles.observe(this, Observer { rssArticles ->
                if (rssArticles != null) {
                    allArticles.addAll(rssArticles)
                }

                // Sort all articles by date (newest first)
                allArticles.sortByDescending { article ->
                    when (article) {
                        is RssArticle -> article.getDate() ?: Date(0) // Default to epoch if null
                        else -> Date(0) // Fallback case
                    }
                }

                // Update the adapter with sorted articles
                articleAdapter.updateArticles(allArticles)
            })

            // After adding steam articles, update the adapter
            articleAdapter.updateArticles(allArticles)
        }
    }

    // Get RSS URLs for the games
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
