package com.example.playfeed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var followedGamesAdapter: FollowedGamesAdapter
    private val followedGames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        setupUserInfo()
        setupFollowedGames()
        setupButtonListeners()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeButton -> {
                    navigateToActivity(HomeActivity::class.java)
                    true
                }
                R.id.searchButton -> {
                    navigateToActivity(CategoriesActivity::class.java)
                    true
                }
                R.id.userButton -> {
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.userButton
    }



    private fun setupUserInfo() {
        auth.currentUser?.let { user ->
            findViewById<TextView>(R.id.tvUserName).text = user.displayName ?: "No name set"
            findViewById<TextView>(R.id.tvUserEmail).text = user.email ?: "No email set"
        }
    }

    private fun setupFollowedGames() {
        val followedGamesRecyclerView = findViewById<RecyclerView>(R.id.followedGamesRecyclerView)
        followedGamesRecyclerView.layoutManager = LinearLayoutManager(this)
        followedGamesAdapter = FollowedGamesAdapter(followedGames) { game -> unfollowGame(game) }
        followedGamesRecyclerView.adapter = followedGamesAdapter

        loadFollowedGames()
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener { changePassword() }
        findViewById<Button>(R.id.signOutButton).setOnClickListener { signOut() }
        findViewById<Button>(R.id.deleteAccountButton).setOnClickListener { deleteAccount() }
    }

    private fun loadFollowedGames() {
        auth.currentUser?.uid?.let { uid ->
            database.child("users").child(uid).child("followed_games")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        followedGames.clear()
                        snapshot.children.mapNotNullTo(followedGames) { it.key }
                        followedGamesAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Failed to load followed games")
                    }
                })
        }
    }

    private fun changePassword() {
        val currentPassword = findViewById<TextInputEditText>(R.id.etCurrentPassword).text.toString()
        val newPassword = findViewById<TextInputEditText>(R.id.etNewPassword).text.toString()

        when {
            currentPassword.isEmpty() || newPassword.isEmpty() ->
                showToast("Please fill all fields")
            newPassword.length < 6 ->
                showToast("Password must be at least 6 characters")
            else -> authenticateAndUpdatePassword(currentPassword, newPassword)
        }
    }

    private fun authenticateAndUpdatePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)

        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        showToast("Password updated successfully. Please login again.")
                        clearPasswordFields()
                        signOut() // Add this line to trigger sign out
                    } else {
                        showToast("Update failed: ${updateTask.exception?.message}")
                    }
                }
            } else {
                showToast("Authentication failed: ${task.exception?.message}")
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        navigateTo(LoginActivity::class.java)
        finish()
    }
    private fun unfollowGame(game: String) {
        auth.currentUser?.uid?.let { uid ->
            database.child("users").child(uid).child("followed_games").child(game)
                .removeValue().addOnCompleteListener { task ->
                    val message = if (task.isSuccessful) "Unfollowed $game" else "Failed to unfollow"
                    showToast(message)
                }
        }
    }


    private fun deleteAccount() {
        auth.currentUser?.let { user ->
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account!")
                .setPositiveButton("Delete") { _, _ ->
                    user.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navigateTo(LoginActivity::class.java)
                            finish()
                        }
                        showToast(task.exception?.message ?: "Account deletion ${if (task.isSuccessful) "successful" else "failed"}")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun clearPasswordFields() {
        findViewById<TextInputEditText>(R.id.etCurrentPassword).text?.clear()
        findViewById<TextInputEditText>(R.id.etNewPassword).text?.clear()
    }

    private fun navigateTo(cls: Class<*>) {
        startActivity(Intent(this, cls))
        overridePendingTransition(0, 0)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    class FollowedGamesAdapter(
        private var games: List<String>,
        private val onUnfollow: (String) -> Unit
    ) : RecyclerView.Adapter<FollowedGamesAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val gameName: TextView = itemView.findViewById(R.id.gameName)
            val unfollowButton: Button = itemView.findViewById(R.id.unfollowButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_followed_game, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.gameName.text = games[position]
            holder.unfollowButton.setOnClickListener { onUnfollow(games[position]) }
        }

        override fun getItemCount() = games.size

        fun updateGames(newGames: List<String>) {
            (games as MutableList).apply {
                clear()
                addAll(newGames)
            }
            notifyDataSetChanged()
        }

    }
}