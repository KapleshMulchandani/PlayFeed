package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playfeed.GameDetailActivity
import com.example.playfeed.HomeActivity
import com.example.playfeed.R
import com.example.playfeed.UserActivity

class CategoriesActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var userButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize navigation buttons
        homeButton = findViewById(R.id.homeButton)
        searchButton = findViewById(R.id.searchButton)
        userButton = findViewById(R.id.userButton)

        // Set navigation click listeners
        homeButton.setOnClickListener { navigateToActivity(HomeActivity::class.java) }
        searchButton.setOnClickListener { navigateToActivity(CategoriesActivity::class.java) }
        userButton.setOnClickListener { navigateToActivity(UserActivity::class.java) }

        // Initialize category images
        setupCategoryClickListeners()
    }

    private fun setupCategoryClickListeners() {
        findViewById<ImageView>(R.id.category1Image).setOnClickListener {
            openGameDetails("CS2", R.drawable.cs2)
        }

        findViewById<ImageView>(R.id.category2Image).setOnClickListener {
            openGameDetails("Valorant", R.drawable.valorant)
        }

        findViewById<ImageView>(R.id.category3Image).setOnClickListener {
            openGameDetails("League of Legends", R.drawable.lol)
        }

        findViewById<ImageView>(R.id.category4Image).setOnClickListener {
            openGameDetails("Dota 2", R.drawable.dota2)
        }

        findViewById<ImageView>(R.id.category5Image).setOnClickListener {
            openGameDetails("Rainbow Six Siege", R.drawable.r6s)
        }

        findViewById<ImageView>(R.id.category6Image).setOnClickListener {
            openGameDetails("Coming Soon", R.drawable.comingsoon)
        }
    }

    private fun openGameDetails(gameName: String, gameImage: Int) {
        val intent = Intent(this, GameDetailActivity::class.java)
        intent.putExtra("GAME_NAME", gameName)
        intent.putExtra("GAME_IMAGE", gameImage)
        startActivity(intent)
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}
