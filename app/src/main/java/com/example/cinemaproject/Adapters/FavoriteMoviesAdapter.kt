package com.example.cinemaproject.Adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FavoriteMoviesAdapter(private val context: Context, private val favoriteMovies: List<Movie>) :
    RecyclerView.Adapter<FavoriteMoviesAdapter.ViewHolder>() {

    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userRef: DatabaseReference

    init {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        userRef = database.getReference("users").child(userId!!).child("liked_movies")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_movie_list_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = favoriteMovies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int {
        return favoriteMovies.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieCardName: TextView = itemView.findViewById(R.id.movieCardName)
        private val movieCardDescription: TextView = itemView.findViewById(R.id.movieCardDescription)
        private val movieCardImage: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(movie: Movie) {
            movieCardName.text = movie.title
            movieCardDescription.text = movie.overview

            movie.posterPath?.let {
                Glide.with(context)
                    .load(it)
                    .placeholder(R.drawable.movie_app_logo)
                    .into(movieCardImage)
            }

            itemView.setOnLongClickListener {
                showDeleteConfirmationDialog(movie)
                true
            }
        }
    }

    private fun showDeleteConfirmationDialog(movie: Movie) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.delete_question)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                deleteMovie(movie)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteMovie(movie: Movie) {
        userRef.child(movie.id.toString()).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Movie deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to remove movie: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
