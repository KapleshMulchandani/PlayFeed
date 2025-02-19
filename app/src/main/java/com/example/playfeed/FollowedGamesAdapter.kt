package com.example.playfeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FollowedGamesAdapter(
    private var games: List<String>,
    private val onUnfollow: (String) -> Unit
) : RecyclerView.Adapter<FollowedGamesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameName: TextView = itemView.findViewById(R.id.gameName)
        val unfollowButton: Button = itemView.findViewById(R.id.unfollowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_followed_game, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = games[position]
        holder.gameName.text = game
        holder.unfollowButton.setOnClickListener {
            onUnfollow(game)
        }
    }

    override fun getItemCount(): Int = games.size

    fun updateGames(newGames: List<String>) {
        (games as MutableList).clear()
        (games as MutableList).addAll(newGames)
        notifyDataSetChanged()
    }
}