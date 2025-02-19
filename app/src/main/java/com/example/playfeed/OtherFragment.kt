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


        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)


        swipeRefreshLayout.setOnRefreshListener {
            fetchArticles()
        }

        // Initialize RecyclerView and Adapter
        recyclerView = rootView.findViewById(R.id.articleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        articleAdapter = ArticleAdapter(emptyList()) // Start with an empty list
        recyclerView.adapter = articleAdapter


        rssViewModel = ViewModelProvider(this).get(RssViewModel::class.java)


        fetchArticles()

        return rootView
    }

    private fun fetchArticles() {
        val allArticles = mutableListOf<RssArticle>()
        val seenTitles = HashSet<String>()


        feedUrls.forEach { url ->
            rssViewModel.fetchRss(url)
        }


        rssViewModel.articles.observe(viewLifecycleOwner, Observer { rssArticles ->
            Log.d("OtherFragment", "Fetched Articles: ${rssArticles?.size}")

            rssArticles?.forEach { article ->

                if (article.title !in seenTitles) {
                    seenTitles.add(article.title)
                    allArticles.add(article)
                }
            }


            allArticles.sortByDescending { it.getDate() ?: Date(0) }


            articleAdapter.updateArticles(allArticles)

            Log.d("OtherFragment", "Final Articles: ${allArticles.size}")


            swipeRefreshLayout.isRefreshing = false
        })
    }
}
