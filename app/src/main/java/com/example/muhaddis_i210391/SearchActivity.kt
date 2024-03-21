package com.example.muhaddis_i210391

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchActivity : AppCompatActivity() {

    private lateinit var recentSearches: MutableList<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var llRecentSearches: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sharedPreferences = getSharedPreferences("RecentSearches", Context.MODE_PRIVATE)
        llRecentSearches = findViewById(R.id.llRecentSearches)

        // Load recent searches from SharedPreferences
        loadRecentSearches()

        val backArrow = findViewById<ImageView>(R.id.backArrow).apply {
            setOnClickListener {
                navigateToMenu()
            }
        }

        val searchBar = findViewById<EditText>(R.id.searchBar)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView).apply {
            selectedItemId = R.id.navigation_search
            setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        navigateToMenu()
                        true
                    }
                    R.id.navigation_chat -> {
                        startActivity(Intent(this@SearchActivity, ChatsActivity::class.java))
                        true
                    }
                    R.id.navigation_add -> {
                        startActivity(Intent(this@SearchActivity, AddMentorActivity::class.java))
                        true
                    }
                    R.id.navigation_profile -> {
                        startActivity(Intent(this@SearchActivity, MyProfileActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        searchBar.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(textView.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotBlank()) {
            saveSearchQuery(query)
            val intent = Intent(this, SearchResultsActivity::class.java).apply {
                putExtra("query", query.trim())
            }
            startActivity(intent)
        }
    }

    private fun navigateToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    private fun saveSearchQuery(query: String) {
        val recentSearchesSet = getRecentSearches().toMutableSet()

        // Remove query if it already exists to avoid duplicates and to update its position
        if (recentSearchesSet.contains(query)) {
            recentSearchesSet.remove(query)
        }

        // Convert back to a mutable list to manage the insertion order and limit
        val recentSearchesList = recentSearchesSet.toMutableList()

        // Add the new search query at the beginning of the list
        recentSearchesList.add(0, query)

        // Limit the number of recent searches to keep
        if (recentSearchesList.size > 5) {
            recentSearchesList.removeAt(recentSearchesList.lastIndex) // Remove the oldest search query
        }

        // Save the updated list back to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putStringSet("recentSearches", recentSearchesList.toSet())
        editor.apply()

        // Update UI
        updateRecentSearchesUI(recentSearchesList)
    }


    private fun loadRecentSearches() {
        val recentSearches = getRecentSearches().toList()
        updateRecentSearchesUI(recentSearches)
    }

    private fun getRecentSearches(): Set<String> {
        return sharedPreferences.getStringSet("recentSearches", emptySet()) ?: emptySet()
    }

    private fun updateRecentSearchesUI(recentSearches: List<String>) {
        llRecentSearches.removeAllViews()
        recentSearches.forEach { search ->
            // Create a horizontal LinearLayout for each search item
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            // TextView for the search term
            val textView = TextView(this).apply {
                text = search
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // ImageView for the cross icon
            val crossImageView = ImageView(this).apply {
                setImageResource(R.drawable.cross) // Make sure you have a cross icon drawable
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(16, 16, 16, 16) // Adjust padding as needed
                setOnClickListener {
                    // Remove this search term from recent searches
                    removeSearchQuery(search)
                }
            }

            // Add TextView and ImageView to the item layout
            itemLayout.addView(textView)
            itemLayout.addView(crossImageView)

            // Add the item layout to the parent LinearLayout
            llRecentSearches.addView(itemLayout)
        }
    }

    private fun removeSearchQuery(query: String) {
        val recentSearchesSet = getRecentSearches().toMutableSet()
        recentSearchesSet.remove(query)

        // Save the updated list back to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putStringSet("recentSearches", recentSearchesSet)
        editor.apply()

        // Update UI with the updated list
        updateRecentSearchesUI(recentSearchesSet.toList())
    }



}
