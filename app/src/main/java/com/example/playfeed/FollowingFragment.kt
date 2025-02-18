package com.example.playfeed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

class FollowingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var followedGames: List<String> = listOf() // This will be populated from Firebase

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_following, container, false)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)

        // Set up the pull-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener {
            fetchArticles() // Trigger data fetch when user pulls to refresh
        }

        // Initialize RecyclerView and Adapter
        recyclerView = rootView.findViewById(R.id.articleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter

        // Initialize ViewModel
        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)

        // Fetch the user's followed games from Firebase
        fetchFollowedGames()

        return rootView
    }

    private fun fetchFollowedGames() {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            // Get the followed games from Firebase
            val userRef = firebaseDatabase.getReference("users").child(currentUserId).child("followed_games")
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Get followed games keys as a list of game names (which are the keys)
                    followedGames = snapshot.children.mapNotNull { it.key }
                    Log.d("FollowingFragment", "Followed Games: $followedGames")

                    // Once we have the followed games, fetch the articles
                    fetchArticles()
                } else {
                    Log.d("FollowingFragment", "No followed games found for user.")
                }
            }.addOnFailureListener { exception ->
                Log.e("FollowingFragment", "Error fetching followed games: ", exception)
            }
        } else {
            Log.d("FollowingFragment", "User is not logged in.")
        }
    }

    private fun fetchArticles() {
        val allArticles = mutableListOf<RssArticle>()
        val seenTitles = HashSet<String>() // To keep track of unique article titles

        // Fetch feeds for each followed game
        followedGames.forEach { gameName ->
            val gameRssUrls = getRssUrls(gameName)
            gameRssUrls.forEach { url ->
                rssViewModel.fetchRss(url)
            }
        }

        // Observe articles from the ViewModel
        rssViewModel.articles.observe(viewLifecycleOwner, Observer { rssArticles ->
            Log.d("FollowingFragment", "Fetched Articles: ${rssArticles?.size}")

            rssArticles?.forEach { article ->
                // Add only unique articles based on the title
                if (article.title !in seenTitles) {
                    seenTitles.add(article.title)
                    allArticles.add(article)
                }
            }

            // Sort articles by date (newest first)
            allArticles.sortByDescending { it.getDate() ?: Date(0) }

            // Update the adapter with the fetched articles
            articleAdapter.updateArticles(allArticles)

            Log.d("FollowingFragment", "Final Articles: ${allArticles.size}")

            // Stop refreshing once articles are fetched
            swipeRefreshLayout.isRefreshing = false
        })
    }

    // Function to return RSS URLs based on the followed game name
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
            else -> emptyList() // No source if no match
        }
    }
}
