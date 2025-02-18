package com.example.playfeed

import com.example.playfeed.CategoriesActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class HomeActivity : AppCompatActivity() {
    private lateinit var homeButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var userButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // This line refers to the RelativeLayout with ID 'main'
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        homeButton = findViewById(R.id.homeButton)
        searchButton = findViewById(R.id.searchButton)
        userButton = findViewById(R.id.userButton)

        // Set click listeners to navigate between activities
        homeButton.setOnClickListener {
            navigateToActivity(HomeActivity::class.java) // Replace Activity1 with your actual activity class
        }

        searchButton.setOnClickListener {
            navigateToActivity(CategoriesActivity::class.java) // Replace Activity2 with your actual activity class
        }

        userButton.setOnClickListener {
            navigateToActivity(UserActivity::class.java) // Replace Activity3 with your actual activity class
        }
    }
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

}

