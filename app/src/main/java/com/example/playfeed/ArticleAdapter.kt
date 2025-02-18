package com.example.playfeed

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.playfeed.R

class ArticleAdapter(private var articles: List<Article>) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // Update the list of articles
    fun updateArticles(newArticles: List<Article>) {
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

        fun bind(article: Article) {
            when (article) {
                is Article.RssArticle -> {
                    // Bind RSS article
                    articleTitle.text = article.title
                    if (article.imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(R.drawable.hltv).into(articleImage) // Fallback image
                    } else {
                        Picasso.get().load(article.imageUrl).into(articleImage)
                    }
                }
                is Article.SteamArticle -> {
                    // Bind Steam article
                    articleTitle.text = article.title
                    Picasso.get().load(R.drawable.steam).into(articleImage) // Use steam image for Steam articles
                }
            }

            // Set up click listener for the image and title
            val url = when (article) {
                is Article.RssArticle -> article.link
                is Article.SteamArticle -> article.url
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
