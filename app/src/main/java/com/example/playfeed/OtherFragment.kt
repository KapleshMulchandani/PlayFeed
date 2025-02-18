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
import java.util.Date

class OtherFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var rssViewModel: RssViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Predefined list of RSS feed URLs
    private val feedUrls = listOf(
        "https://www.oneesports.gg/gaming/feed/",
        "https://dotesports.com/reviews/feed",
        "https://esportsinsider.com/features/insights/feed"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_other, container, false)

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

        // Fetch articles immediately when the fragment is created
        fetchArticles()

        return rootView
    }

    private fun fetchArticles() {
        val allArticles = mutableListOf<RssArticle>()
        val seenTitles = HashSet<String>() // To keep track of unique article titles

        // For each feed URL in the predefined list, fetch the articles
        feedUrls.forEach { url ->
            rssViewModel.fetchRss(url)
        }

        // Observe articles from the ViewModel
        rssViewModel.articles.observe(viewLifecycleOwner, Observer { rssArticles ->
            Log.d("OtherFragment", "Fetched Articles: ${rssArticles?.size}")

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

            Log.d("OtherFragment", "Final Articles: ${allArticles.size}")

            // Stop refreshing once articles are fetched
            swipeRefreshLayout.isRefreshing = false
        })
    }
}
