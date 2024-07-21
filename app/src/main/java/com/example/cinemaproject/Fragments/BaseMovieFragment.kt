package com.example.cinemaproject.Fragments

import MovieApiService
import RetrofitInstance
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemaproject.Adapters.MoviesAdapter
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.R
import com.example.cinemaproject.ViewModel.MoviesViewModel
import com.example.cinemaproject.databinding.CardMovieDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseMovieFragment : Fragment() {
    protected lateinit var moviesAdapter: MoviesAdapter
    protected val viewModel: MoviesViewModel by viewModels()
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid
    protected val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "")
    private lateinit var likedMoviesListener: ValueEventListener
    protected val likedMoviesMap = HashMap<Int, Boolean>()
    protected val apiKey = "X"

    protected abstract val recyclerView: RecyclerView

    protected abstract fun observeMovies()
    protected abstract fun fetchMovies()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return getFragmentView(inflater, container)
    }

    abstract fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeMovies()
        fetchMovies()
        fetchLikedMovies()
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter(emptyList()) { movie -> showMovieDetails(movie) }
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = moviesAdapter
        }
    }

    protected fun fetchLikedMovies() {
        likedMoviesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likedMoviesMap.clear()
                snapshot.child("liked_movies").children.forEach { movieSnapshot ->
                    val movieId = movieSnapshot.key?.toIntOrNull()
                    movieId?.let { likedMoviesMap[it] = true }
                }
                updateMovieLikedStates()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch liked movies: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        userRef.addListenerForSingleValueEvent(likedMoviesListener)
    }

    protected fun updateMovieLikedStates() {
        viewModel.movies.value?.forEach { movie -> movie.isLiked = likedMoviesMap.containsKey(movie.id) }
        moviesAdapter.notifyDataSetChanged()
    }

    protected fun showMovieDetails(movie: Movie) {
        val dialog = Dialog(requireContext())
        val dialogBinding = CardMovieDetailsBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.MovieCardName.text = movie.title
        dialogBinding.MovieCardYear.text = movie.release_date
        dialogBinding.MovieCardDescription.text = movie.overview

        movie.posterPath?.let { imageUrl ->
            Glide.with(requireContext())
                .load("https://image.tmdb.org/t/p/w500$imageUrl")
                .into(dialogBinding.MovieCardImage)
        }

        checkLikedStatusAndUpdateUI(movie, dialogBinding)

        dialogBinding.likeButton.setOnClickListener {
            val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim)
            dialogBinding.likeButton.startAnimation(scaleAnimation)
            handleLikeButtonClick(movie, dialogBinding.likeButton)
        }

        fetchTrailerVideoAndUpdateMovie(movie, dialogBinding)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    private fun checkLikedStatusAndUpdateUI(movie: Movie, dialogBinding: CardMovieDetailsBinding) {
        userRef.child("liked_movies").child(movie.id.toString()).get().addOnSuccessListener { dataSnapshot ->
            movie.isLiked = dataSnapshot.exists()
            dialogBinding.likeButton.setImageResource(if (movie.isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_empty)
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Error checking liked status: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLikeButtonClick(movie: Movie, likeButton: ImageView) {
        if (movie.isLiked) {
            movie.isLiked = false
            likeButton.setImageResource(R.drawable.ic_heart_empty)
            userRef.child("liked_movies").child(movie.id.toString()).removeValue()
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to remove movie: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            movie.isLiked = true
            likeButton.setImageResource(R.drawable.ic_heart_full)
            saveLikedMovie(movie)
        }
        updateMovieLikedStates()
    }

    private fun saveLikedMovie(movie: Movie) {
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val likedMovieData = mapOf(
                    "name" to movie.title,
                    "poster_link" to "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                    "release_date" to movie.release_date,
                    "trailer_url" to (movie.trailerUrl ?: ""),
                    "description" to movie.overview
                )
                userRef.child("liked_movies").child(movie.id.toString()).setValue(likedMovieData)
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save movie: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    protected fun fetchTrailerVideoAndUpdateMovie(movie: Movie, dialogBinding: CardMovieDetailsBinding) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.retrofit.create(MovieApiService::class.java).getMovieVideos(movie.id, apiKey)
                val trailer = response.videos.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
                withContext(Dispatchers.Main) {
                    trailer?.let {
                        movie.trailerUrl = "https://www.youtube.com/watch?v=${it.key}"
                        dialogBinding.MovieCardTrailerLink.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.cueVideo(it.key, 0f)
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                Toast.makeText(requireContext(), "Error loading trailer", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } ?: Toast.makeText(requireContext(), "No trailer available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(requireContext(), "Failed to fetch trailer", Toast.LENGTH_SHORT).show() }
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userRef.removeEventListener(likedMoviesListener)
    }
}
