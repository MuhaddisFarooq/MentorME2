package com.example.muhaddis_i210391


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchResultsActivity : AppCompatActivity() {
    private lateinit var specialtiesSpinner: Spinner
    private var allMentors: MutableList<Mentorr> = mutableListOf()
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        setupBottomNavigationView()
        setupSpinner()

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener(){
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvSearchResults).apply {
            layoutManager = LinearLayoutManager(this@SearchResultsActivity)
        }

        // Initialize the adapter with an empty list and set it to the RecyclerView
        adapter = SearchResultsAdapter(listOf()) { selectedMentor ->
            // Navigate to MentorProfileActivity with the selected mentor's details
            val intent = Intent(this@SearchResultsActivity, MentorProfileActivity::class.java).apply {
                putExtra("MENTOR_ID", selectedMentor.id) // Pass the mentor's ID
                putExtra("MENTOR_NAME", selectedMentor.name)
                putExtra("MENTOR_DESCRIPTION", selectedMentor.description)
                putExtra("MENTOR_PRICE", selectedMentor.price)
                putExtra("MENTOR_IMAGE_URL", selectedMentor.imageUrl)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val query = intent.getStringExtra("query") ?: ""
        fetchMentors(query)
    }


    private fun filterMentors(selectedSpecialty: String) {
        val filteredMentors = if (selectedSpecialty == "Filter") {
            allMentors
        } else {
            allMentors.filter { it.description == selectedSpecialty }
        }
        adapter.updateData(filteredMentors)
    }

    private fun fetchMentors(query: String) {
        val mentorRef = FirebaseDatabase.getInstance().getReference("Mentors")
        mentorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mentors = mutableListOf<Mentorr>()
                snapshot.children.forEach { child ->
                    val mentor = child.getValue(Mentorr::class.java)
                    if (mentor != null && mentor.name.contains(query, ignoreCase = true)) {
                        mentors.add(mentor)
                    }
                }
                // Update the adapter with the fetched mentors
                adapter.updateData(mentors)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })
    }

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_search
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                R.id.navigation_search -> true // Current Activity
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddMentorActivity::class.java))
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }



    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.btnFilter)
        val categories = listOf("Filter", "UX/UI Designer", "Software Engineer", "Product Manager")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        // Set listener for spinner item selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Filter logic here
                // Ignore first item which is "Filter"
                if (position > 0) {
                    val selectedCategory = parent.getItemAtPosition(position).toString()
                    // Implement your filtering logic here
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Sometimes you need nothing here
            }
        }
    }

}
