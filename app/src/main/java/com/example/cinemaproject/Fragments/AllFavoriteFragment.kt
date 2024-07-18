package com.example.cinemaproject.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinemaproject.Adapters.FavoriteMoviesAdapter
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.databinding.FavoriteLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllFavoriteFragment : Fragment() {
    private var _binding: FavoriteLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var favoriteMoviesAdapter: FavoriteMoviesAdapter
    private lateinit var favoriteMoviesList: MutableList<Movie>
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser?.uid
    private var userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("liked_movies")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FavoriteLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        binding.recyclerViewFavorite.layoutManager = LinearLayoutManager(requireContext())
        favoriteMoviesList = mutableListOf()
        favoriteMoviesAdapter = FavoriteMoviesAdapter(requireContext(), favoriteMoviesList)
        binding.recyclerViewFavorite.adapter = favoriteMoviesAdapter

        loadFavoriteMovies()

        val deleteAllIcon = binding.deleteAllIcon
        deleteAllIcon.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }
    }

    private fun loadFavoriteMovies() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val userRef = database.getReference("users").child(userId!!).child("liked_movies")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteMoviesList.clear()
                for (movieSnapshot in snapshot.children) {
                    val movieId = movieSnapshot.key  // This retrieves the movieId (e.g., "762441")
                    val releaseDate = movieSnapshot.child("release_date").getValue(String::class.java)
                    val title = movieSnapshot.child("name").getValue(String::class.java)
                    val overview = movieSnapshot.child("description").getValue(String::class.java)
                    val posterPath = movieSnapshot.child("poster_link").getValue(String::class.java)
                    val trailerUrl = movieSnapshot.child("trailer_url").getValue(String::class.java)

                    // Ensure essential fields are not null before creating Movie object
                    if (movieId != null && releaseDate != null && title != null && overview != null) {
                        val movie = Movie(
                            id = movieId.toInt(),  // Convert movieId to Int if it's stored as String
                            release_date = releaseDate,
                            title = title,
                            overview = overview,
                            posterPath = posterPath ?: "",  // Handle nullable posterPath
                            trailerUrl = trailerUrl,
                            isLiked = true,  // Example of setting isLiked
                            userId = userId  // Example of setting userId
                        )
                        favoriteMoviesList.add(movie)
                    }
                }
                favoriteMoviesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun showDeleteAllConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Delete All")
            .setMessage("Are you sure you want to delete all liked movies?")
            .setPositiveButton("Delete") { dialog, which ->
                // Delete all movies
                deleteAllMovies()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAllMovies() {
        userRef.removeValue()
            .addOnSuccessListener {
                favoriteMoviesList.clear()
                favoriteMoviesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
