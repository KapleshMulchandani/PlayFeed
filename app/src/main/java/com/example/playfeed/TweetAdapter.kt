package com.example.playfeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import twitter4j.Status


class TweetAdapter(private var tweetList: List<Status>) :
    RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tweet, parent, false)
        return TweetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val tweet = tweetList[position]
        holder.titleTextView.text = tweet.text
        // You can load an image here if the tweet has any media (optional)
        // For example, use Picasso or Glide to load images if available
    }

    override fun getItemCount(): Int = tweetList.size

    // Update the list of tweets when new data is fetched
    fun updateTweets(newTweets: List<Status>) {
        tweetList = newTweets
        notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
    }

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.articleTitle)
        val articleImageView: ImageView = itemView.findViewById(R.id.articleImage)
    }
}
