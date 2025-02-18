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

class ArticleAdapter(private var articles: List<RssArticle>) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // Update the list of articles
    fun updateArticles(newArticles: List<RssArticle>) {
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

        fun bind(article: RssArticle) {
            // Set article title
            articleTitle.text = article.title

            // Check if the image URL is valid, else use a fallback image
            val imageUrl = article.mediaUrl
            if (imageUrl.isNullOrEmpty()) {
                Picasso.get().load(R.drawable.hltv).into(articleImage) // Fallback image
            } else {
                Picasso.get().load(imageUrl).into(articleImage)
            }

            // Set up click listener for the image and title
            val url = article.link

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