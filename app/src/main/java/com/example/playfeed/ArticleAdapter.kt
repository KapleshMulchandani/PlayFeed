package com.example.playfeed

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ArticleAdapter(private var articles: List<Any>) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // Update the list of articles and notify the adapter
    fun updateArticles(newArticles: List<Any>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = articles.size

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
        private val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)
        private val articleDate: TextView = itemView.findViewById(R.id.articleDate)
        private val sourceText: TextView = itemView.findViewById(R.id.sourceText)

        fun bind(article: Any) {
            when (article) {
                is RssArticle -> {
                    articleTitle.text = article.title

                    // Debug: Log the article link
                    Log.d("ArticleLink", "Article link: ${article.link}")

                    // Set image based on article link
                    when {
                        article.link.contains("vlr.gg") -> {
                            Picasso.get().load(R.drawable.vlr).into(articleImage)
                        }
                        article.link.contains("hltv.org") -> {
                            Picasso.get().load(R.drawable.hltv).into(articleImage)
                        }
                        article.link.contains("counter-strike.net/news/updates") -> {
                            Picasso.get().load(R.drawable.steam).into(articleImage)
                        }
                        else -> {
                            // Log the image URL
                            Log.d("ImageURL", "Image URL: ${article.imageUrl}")
                            if (article.imageUrl.isNullOrEmpty()) {
                                Picasso.get().load(R.drawable.cantfindimage).into(articleImage)
                            } else {
                                Picasso.get().load(article.imageUrl).into(articleImage)
                            }
                        }
                    }

                    // Format publication date and set it to the TextView
                    val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                    val date = try { inputFormat.parse(article.pubDate) } catch (e: Exception) { null }
                    articleDate.text = date?.let { outputFormat.format(it) } ?: ""

                    // Set the source text dynamically (based on the article's source)
                    val source = when {
                        article.link.contains("dexerto", ignoreCase = true) -> "Dexerto"
                        article.link.contains("dotesports", ignoreCase = true) -> "Dot Esports"
                        article.link.contains("oneesports", ignoreCase = true) -> "One Esports"
                        article.link.contains("esportsinsider", ignoreCase = true) -> "Esports Insider"
                        article.link.contains("github", ignoreCase = true) -> "Official"
                        article.link.contains("hltv", ignoreCase = true) -> "HLTV"
                        else -> "Other Source"
                    }
                    sourceText.text = source

                    // Set click listener for the source text to open the article link
                    sourceText.setOnClickListener {
                        // Open the article's URL (same as title and image)
                        val context = itemView.context
                        val openSourceIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                        context.startActivity(openSourceIntent)
                    }
                }
                else -> {
                    // Optionally handle other types if needed
                    articleTitle.text = "Unknown article type"
                }
            }

            // Click listener to open article link (same behavior for image and title)
            val url = when (article) {
                is RssArticle -> article.link
                else -> ""
            }
            val context = itemView.context
            val openUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            articleImage.setOnClickListener { context.startActivity(openUrlIntent) }
            articleTitle.setOnClickListener { context.startActivity(openUrlIntent) }
        }
    }
}
