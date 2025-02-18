package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel
    private lateinit var homeButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var userButton: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUser = firebaseAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) // Initialize swipeRefreshLayout

        swipeRefreshLayout.setOnRefreshListener {
            // Trigger the data fetch when user pulls to refresh
            fetchArticles()
        }

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        homeButton = findViewById(R.id.homeButton)
        searchButton = findViewById(R.id.searchButton)
        userButton = findViewById(R.id.userButton)

        homeButton.setOnClickListener {
            navigateToActivity(HomeActivity::class.java) // Navigate to HomeActivity
        }

        searchButton.setOnClickListener {
            navigateToActivity(CategoriesActivity::class.java) // Navigate to CategoriesActivity
        }

        userButton.setOnClickListener {
            navigateToActivity(UserActivity::class.java) // Navigate to UserActivity
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.followedGamesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter

        // Initialize ViewModel
        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)

        // Fetch articles (future-proof: supports all/followed games)
        fetchArticles()
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    private fun fetchArticles() {
        val allArticles = mutableListOf<RssArticle>()
        val seenTitles = HashSet<String>() // Track unique article titles

        // Fetch followed games from Firebase
        currentUser?.uid?.let { uid ->
            val followedGamesRef = database.child("users").child(uid).child("followed_games")
            Log.d("HomeActivity", "Fetching followed games from: ${followedGamesRef.path}")

            followedGamesRef.get()
                .addOnSuccessListener { snapshot ->
                    // Get all game names (keys of the map) as a list
                    val followedGames = snapshot.children.mapNotNull { it.key }

                    // Debug: Log the fetched followed games
                    Log.d("HomeActivity", "Fetched Followed Games: $followedGames")

                    if (followedGames.isNotEmpty()) {
                        val feedUrls = followedGames.flatMap { game -> getRssUrls(game) }

                        // Debug: Log the RSS feed URLs
                        Log.d("HomeActivity", "Feed URLs: $feedUrls")

                        for (url in feedUrls) {
                            rssViewModel.fetchRss(url)
                        }

                        // Observe articles from ViewModel
                        rssViewModel.articles.observe(this, Observer { rssArticles ->
                            Log.d("HomeActivity", "Fetched Articles: ${rssArticles?.size}")

                            // Add unique articles
                            rssArticles?.forEach { article ->
                                if (article.title !in seenTitles) {  // Only add unique articles
                                    seenTitles.add(article.title)
                                    allArticles.add(article)
                                }
                            }

                            // Sort by date (newest first)
                            allArticles.sortByDescending { it.getDate() ?: Date(0) }

                            // Update adapter
                            articleAdapter.updateArticles(allArticles)

                            Log.d("HomeActivity", "Final Articles: ${allArticles.size}")

                            // Stop refreshing after loading the articles
                            swipeRefreshLayout.isRefreshing = false
                        })
                    } else {
                        Log.d("HomeActivity", "No followed games found.")
                        swipeRefreshLayout.isRefreshing = false  // Stop refreshing if no games found
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeActivity", "Error fetching followed games", exception)
                    swipeRefreshLayout.isRefreshing = false  // Stop refreshing on failure
                }
        }
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
                "https://esportsinsider.com/esports-titles/shooters/valorant/feed"
            )
            "league of legends" -> listOf(
                "https://www.dexerto.com/league-of-legends/feed/",
                "https://dotesports.com/league-of-legends/feed",
                "https://www.oneesports.gg/league-of-legends/feed/",
                "https://data.rito.news/lol/en-us/news.rss",
                "https://data.rito.news/lol/en-us/esports.rss",
                "https://esportsinsider.com/esports-titles/moba/lol/feed"
            )
            "dota 2" -> listOf(
                "https://dotesports.com/dota-2/feed",
                "https://www.oneesports.gg/dota2/feed/",
                "https://esportsinsider.com/esports-titles/moba/dota-2/feed"
            )
            "fortnite" -> listOf(
                "https://www.dexerto.com/feed/category/fortnite/",
                "https://dotesports.com/fortnite/feed",
                "https://www.oneesports.gg/fortnite/feed/",
                "https://esportsinsider.com/esports-titles/battle-royale/fortnite/feed"
            )
            "other" -> listOf(
                "https://www.oneesports.gg/gaming/feed/",
                "https://dotesports.com/reviews/feed",
                "https://esportsinsider.com/features/insights/feed"
            )
            else -> emptyList() // No source if no match
        }
    }
}
