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

    // Update the list of articles
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
        private val articleDate: TextView = itemView.findViewById(R.id.articleDate) // Add reference to date TextView

        fun bind(article: Any) {
            when (article) {
                is RssArticle -> {
                    articleTitle.text = article.title

                    // Debug: Log the link to check if it's from VLR, HLTV, or Dexerto
                    Log.d("ArticleLink", "Article link: ${article.link}")

                    when {
                        article.link.contains("vlr.gg") -> {
                            Picasso.get().load(R.drawable.vlr).into(articleImage) // VLR image
                        }

                        article.link.contains("hltv.org") -> {
                            Picasso.get().load(R.drawable.hltv).into(articleImage) // HLTV image
                        }

                        article.link.contains("counter-strike.net/news/updates") -> {
                            Picasso.get().load(R.drawable.steam).into(articleImage) // Steam image
                        }

                        else -> {
                            // Log the image URL to verify if it is correct
                            Log.d("ImageURL", "Image URL: ${article.imageUrl}")

                            // Check if the imageUrl is valid before loading it
                            if (article.imageUrl.isNullOrEmpty()) {
                                Picasso.get().load(R.drawable.cantfindimage)
                                    .into(articleImage) // Fallback image
                            } else {
                                Picasso.get().load(article.imageUrl)
                                    .into(articleImage) // Load image from URL
                            }
                        }
                    }

                    // Format the publication date and set it to the TextView
                    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault()) // Customize the format as needed
                    val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(article.pubDate) // Original date format
                    val formattedDate = date?.let { dateFormat.format(it) }
                    articleDate.text = formattedDate
                }
            }

            // Set up click listener for the image and title
            val url = when (article) {
                is RssArticle -> article.link
                else -> ""
            }
            val context = itemView.context
            val openUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            // Open the URL when the image is clicked
            articleImage.setOnClickListener {
                context.startActivity(openUrlIntent)
            }

            // Open the URL when the title is clicked
            articleTitle.setOnClickListener {
                context.startActivity(openUrlIntent)
            }
        }
    }
}

