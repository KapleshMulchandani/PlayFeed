package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Date


class GameDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel
    private lateinit var followButton: MaterialButton


    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUser = firebaseAuth.currentUser
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)


        val gameName = intent.getStringExtra("GAME_NAME") ?: "Game"
        val gameImageRes = intent.getIntExtra("GAME_IMAGE", R.drawable.ic_launcher_foreground)

        val user = firebaseAuth.currentUser


        findViewById<ImageView>(R.id.gameImage).setImageResource(gameImageRes)
        findViewById<TextView>(R.id.gameName).text = gameName


        followButton = findViewById(R.id.followButton)
        setupFollowButton(gameName)


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter



        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)


        fetchNews(gameName)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupFollowButton(gameName: String) {
        followButton.setOnClickListener {
            val user = firebaseAuth.currentUser
            if (user == null) {
                showLoginDialog() // Show dialog instead of instantly navigating
            } else {
                isFollowing = !isFollowing
                toggleFollowStatus(gameName)
                updateFollowButton()
            }
        }

        firebaseAuth.currentUser?.let {
            checkFollowStatus(gameName) { isFollowed ->
                isFollowing = isFollowed
                updateFollowButton()
            }
        }
    }


    private fun updateFollowButton() {
        if (isFollowing) {
            followButton.text = "Following"
            followButton.icon = getDrawable(R.drawable.following)
            followButton.backgroundTintList = getColorStateList(R.color.secondary)
        } else {
            followButton.text = "Follow"
            followButton.icon = getDrawable(R.drawable.follow)
            followButton.backgroundTintList = getColorStateList(R.color.secondary)
        }
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

                allArticles.sortByDescending { article ->
                    when (article) {
                        is RssArticle -> article.getDate() ?: Date(0)
                        else -> Date(0)
                    }
                }
                articleAdapter.updateArticles(allArticles)
            }
        })
    }


    private fun getRssUrls(gameName: String): List<String> {
        return when (gameName.lowercase()) {
            "counter strike 2", "cs2" -> listOf(
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
            "call of duty" -> listOf(
                "https://www.dexerto.com/feed/category/call-of-duty/",
                "https://esportsinsider.com/esports-titles/shooters/marvel-rivals/feed",
                "https://dotesports.com/call-of-duty/feed"
            )
            "marvel rivals" -> listOf(
                "https://www.dexerto.com/feed/category/marvel-rivals/",
            )
            "overwatch" -> listOf(
                "https://www.dexerto.com/feed/category/overwatch/",
                "https://dotesports.com/overwatch/feed",
                "https://esportsinsider.com/esports-titles/shooters/overwatch/feed"
            )
            "apex legends" -> listOf(
                "https://www.dexerto.com/feed/category/apex-legends/",
                "https://dotesports.com/apex-legends/feed",
                "https://www.oneesports.gg/apex-legends/feed/",
                "https://esportsinsider.com/esports-titles/battle-royale/apex-legends/feed"
            )
            "rocket league" -> listOf(
                "https://www.dexerto.com/feed/category/rocket-league/",
                "https://dotesports.com/rocket-league/feed",
                "https://esportsinsider.com/esports-titles/sports-simulation/rocket-league/feed"
            )
            "rainbow six siege" -> listOf(
                "https://esportsinsider.com/esports-titles/shooters/rainbow-six-siege/feed"
            )
            "pokemon" -> listOf(
                "https://www.dexerto.com/feed/category/pokemon/",
                "https://dotesports.com/pokemon/feed",
                "https://esportsinsider.com/esports-titles/sports-simulation/pokemon/feed"
            )


            "other" -> listOf(
                "https://www.oneesports.gg/gaming/feed/",
                "https://dotesports.com/reviews/feed",
                "https://esportsinsider.com/features/insights/feed"
            )
            else -> emptyList()
        }
    }

    private fun checkFollowStatus(gameName: String, callback: (Boolean) -> Unit) {
        currentUser?.let { user ->
            val userRef = database.child("users").child(user.uid).child("followed_games")
            userRef.child(gameName).get().addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
        }
    }

    private fun toggleFollowStatus(gameName: String) {
        currentUser?.let { user ->
            val userRef = database.child("users").child(user.uid).child("followed_games")
            userRef.child(gameName).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userRef.child(gameName).removeValue()
                } else {
                    userRef.child(gameName).setValue(true)
                }
            }
        }
    }

    private fun showLoginDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Login Required")
            .setMessage("You need to be signed in to follow games. Do you want to log in?")
            .setPositiveButton("Login") { _, _ ->
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
            }
            .setNegativeButton("Cancel", null)
            .show()

        // Change button text colors
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.secondary)) // Change login color
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.error)) // Change cancel color
    }



}