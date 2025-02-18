// GameAdapter.kt
package com.example.playfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameAdapter(private val context: Context, private val gameList: List<String>, private val onGameClickListener: (String) -> Unit) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]
        holder.gameNameTextView.text = game
        holder.itemView.setOnClickListener {
            onGameClickListener(game)
        }
    }

    override fun getItemCount(): Int = gameList.size

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameNameTextView: TextView = itemView.findViewById(android.R.id.text1)
    }
}
