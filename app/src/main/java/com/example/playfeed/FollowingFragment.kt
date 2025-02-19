package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
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
    private lateinit var emptyState: LinearLayout
    private lateinit var exploreButton: Button

    private var followedGames: List<String> = emptyList()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_following, container, false)


        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
        recyclerView = rootView.findViewById(R.id.articleRecyclerView)
        emptyState = rootView.findViewById(R.id.emptyState)
        exploreButton = rootView.findViewById(R.id.exploreGamesButton)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        articleAdapter = ArticleAdapter(emptyList())
        recyclerView.adapter = articleAdapter


        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)


        swipeRefreshLayout.setOnRefreshListener {
            fetchFollowedGames()
        }


        exploreButton.setOnClickListener {
            startActivity(Intent(requireContext(), CategoriesActivity::class.java))
        }


        fetchFollowedGames()
    }

    private fun fetchFollowedGames() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: run {
            showEmptyState()
            return
        }

        firebaseDatabase.getReference("users").child(currentUserId).child("followed_games")
            .get().addOnSuccessListener { snapshot ->
                followedGames = snapshot.children.mapNotNull { it.key }

                if (followedGames.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    fetchArticles()
                }
            }.addOnFailureListener {
                showEmptyState()
            }
    }

    private fun fetchArticles() {
        val allArticles = mutableListOf<RssArticle>()
        val seenTitles = HashSet<String>()

        followedGames.forEach { gameName ->
            getRssUrls(gameName).forEach { url ->
                rssViewModel.fetchRss(url)
            }
        }

        rssViewModel.articles.observe(viewLifecycleOwner) { rssArticles ->
            swipeRefreshLayout.isRefreshing = false

            rssArticles?.forEach { article ->
                if (article.title !in seenTitles) {
                    seenTitles.add(article.title)
                    allArticles.add(article)
                }
            }

            allArticles.sortByDescending { it.getDate() ?: Date(0) }
            articleAdapter.updateArticles(allArticles)


            if (allArticles.isEmpty()) {
                showEmptyState("No articles found for followed games")
            }
        }
    }

    private fun showEmptyState(message: String = "Follow games to see their news!") {
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyState.findViewById<TextView>(R.id.emptyStateText).text = message
    }

    private fun hideEmptyState() {
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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
}
