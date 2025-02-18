package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var userButton: ImageButton

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: FragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Set up the ViewPager2 adapter
        adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2  // Two tabs
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FollowingFragment() // "Following" tab
                    1 -> OtherFragment()     // "Other" tab
                    else -> throw IllegalStateException("Unexpected position $position")
                }
            }
        }

        // Set the adapter to the ViewPager2
        viewPager.adapter = adapter

        // Now attach the TabLayoutMediator after the adapter is set
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Following"
                1 -> "Other"
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }.attach()

        // Handle Window Insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the bottom navigation buttons
        homeButton = findViewById(R.id.homeButton)
        searchButton = findViewById(R.id.searchButton)
        userButton = findViewById(R.id.userButton)

        // Set click listeners to navigate between activities
        homeButton.setOnClickListener {
            navigateToActivity(HomeActivity::class.java)
        }

        searchButton.setOnClickListener {
            navigateToActivity(CategoriesActivity::class.java)
        }

        userButton.setOnClickListener {
            navigateToActivity(UserActivity::class.java)
        }
    }

    // Helper function to navigate to a different activity
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}
