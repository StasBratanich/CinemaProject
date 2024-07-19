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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.cinemaproject.Adapters.MoviesAdapter
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.R
import com.example.cinemaproject.ViewModel.MoviesViewModel
import com.example.cinemaproject.databinding.CardMovieDetailsBinding
import com.example.cinemaproject.databinding.ShowNowLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowNowFragment : Fragment() {

    private var _binding: ShowNowLayoutBinding? = null
    private val binding get() = _binding!!
    private val apiKey = "b947235f7bf13a6bcad6afa6e8e53d2d"
    private lateinit var moviesAdapter: MoviesAdapter
    private val viewModel: MoviesViewModel by viewModels()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "")
    private lateinit var likedMoviesListener: ValueEventListener
    private val likedMoviesMap = HashMap<Int, Boolean>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ShowNowLayoutBinding.inflate(inflater, container, false)
        moviesAdapter = MoviesAdapter(emptyList()) { movie ->
            showMovieDetails(movie)
        }
        binding?.ShowNowRV?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = moviesAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.movies.observe(viewLifecycleOwner, Observer {
            moviesAdapter.updateData(it)
            fetchLikedMovies()
        })
        fetchPopularMovies()
    }

    private fun fetchLikedMovies() {
        likedMoviesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                likedMoviesMap.clear()
                snapshot.child("liked_movies").children.forEach { movieSnapshot ->
                    val movieId = movieSnapshot.key?.toIntOrNull()
                    movieId?.let {
                        likedMoviesMap[it] = true
                    }
                }
                updateMovieLikedStates()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        }

        userRef.addListenerForSingleValueEvent(likedMoviesListener)
    }

    private fun updateMovieLikedStates() {
        viewModel.movies.value?.forEach { movie ->
            movie.isLiked = likedMoviesMap.containsKey(movie.id)
        }
        moviesAdapter.notifyDataSetChanged()
    }

    private fun fetchPopularMovies() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val deferredMovies = (1..5).map { page ->
                    async { RetrofitInstance.retrofit.create(MovieApiService::class.java).getPopularMovies(apiKey, page).movies }
                }
                val allMovies = deferredMovies.awaitAll().flatten()
                withContext(Dispatchers.Main) {
                    allMovies.forEach { viewModel.addMovie(it) }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    private fun showMovieDetails(movie: Movie) {
        val dialog = Dialog(requireContext())
        val dialogBinding = CardMovieDetailsBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.MovieCardName.text = movie.title
        dialogBinding.MovieCardYear.text = movie.release_date
        dialogBinding.MovieCardDescription.text = movie.overview

        movie.posterPath?.let {
            val imageUrl = "https://image.tmdb.org/t/p/w500$it"
            Glide.with(requireContext())
                .load(imageUrl)
                .into(dialogBinding.MovieCardImage)
        }

        dialogBinding.likeButton.setImageResource(if (movie.isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_empty)
        dialogBinding.likeButton.setOnClickListener {
            val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim)
            dialogBinding.likeButton.startAnimation(scaleAnimation)

            if (movie.isLiked) {
                // Movie is currently liked, so unlike it
                movie.isLiked = false
                dialogBinding.likeButton.setImageResource(R.drawable.ic_heart_empty)

                // Remove the movie from Firebase
                userRef.child("liked_movies").child(movie.id.toString()).removeValue()
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to remove movie: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                movie.isLiked = true
                dialogBinding.likeButton.setImageResource(R.drawable.ic_heart_full)

                userRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val likedMovieData = HashMap<String, Any>()
                        likedMovieData["name"] = movie.title
                        likedMovieData["poster_link"] = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
                        likedMovieData["release_date"] = movie.release_date
                        likedMovieData["trailer_url"] = movie.trailerUrl ?: ""
                        likedMovieData["description"] = movie.overview

                        userRef.child("liked_movies").child(movie.id.toString()).setValue(likedMovieData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to save movie: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }

        fetchTrailerVideoAndUpdateMovie(movie, dialogBinding)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(width, height)
        dialog.show()
    }

    private fun fetchTrailerVideoAndUpdateMovie(movie: Movie, dialogBinding: CardMovieDetailsBinding) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.retrofit.create(MovieApiService::class.java)
                    .getMovieVideos(movie.id, apiKey)
                val trailer =
                    response.videos.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
                withContext(Dispatchers.Main) {
                    if (trailer != null) {
                        movie.trailerUrl = "https://www.youtube.com/watch?v=${trailer.key}"
                        dialogBinding.MovieCardTrailerLink.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.cueVideo(trailer.key, 0f)
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                Toast.makeText(requireContext(), "Error loading trailer", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(requireContext(), "No trailer available", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch trailer", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
