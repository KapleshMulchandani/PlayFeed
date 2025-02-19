package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class CategoriesActivity : AppCompatActivity() {

    // Constants for intent extras
    companion object {
        const val EXTRA_GAME_NAME = "GAME_NAME"
        const val EXTRA_GAME_IMAGE = "GAME_IMAGE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set up navigation item selection listener
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeButton -> {
                    navigateToActivity(HomeActivity::class.java)
                    true
                }
                R.id.searchButton -> {
                    // Already in CategoriesActivity, no need to navigate
                    true
                }
                R.id.userButton -> {
                    navigateToActivity(ProfileActivity::class.java)
                    true
                }
                else -> false
            }
        }

        // Set the default selected item (optional)
        bottomNavigationView.selectedItemId = R.id.searchButton

        // Initialize category click listeners
        setupCategoryClickListeners()
    }

    private fun setupCategoryClickListeners() {
        // Map of category IDs to game names and images
        val categories = mapOf(
            R.id.Cs2Image to Pair("Counter Strike 2", R.drawable.cs2),
            R.id.ValorantImage to Pair("Valorant", R.drawable.valorant),
            R.id.LeagueImage to Pair("League of Legends", R.drawable.lol),
            R.id.Dota2Image to Pair("Dota 2", R.drawable.dota2),
            R.id.FortniteImage to Pair("Fortnite", R.drawable.fortnite),
            R.id.CodImage to Pair("Call Of Duty", R.drawable.cod),
            R.id.RivalsImage to Pair("Marvel Rivals", R.drawable.rivals),
            R.id.OverwatchImage to Pair("Overwatch", R.drawable.ow),
            R.id.ApexImage to Pair("Apex Legends", R.drawable.apex),
            R.id.RLImage to Pair("Rocket League", R.drawable.rl),
            R.id.R6SImage to Pair("Rainbow Six Siege", R.drawable.r6s),
            R.id.PokemonImage to Pair("Pokemon", R.drawable.pokemon)
        )

        // Set click listeners for each category
        categories.forEach { (id, gameData) ->
            findViewById<ImageView>(id).setOnClickListener {
                openGameDetails(gameData.first, gameData.second)
            }
        }
    }

    private fun openGameDetails(gameName: String, gameImage: Int) {
        val intent = Intent(this, GameDetailActivity::class.java).apply {
            putExtra(EXTRA_GAME_NAME, gameName)
            putExtra(EXTRA_GAME_IMAGE, gameImage)
        }
        startActivity(intent)
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}