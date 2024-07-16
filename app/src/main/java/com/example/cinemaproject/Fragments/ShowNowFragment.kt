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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowNowFragment : Fragment() {

    private var binding: ShowNowLayoutBinding? = null
    private val apiKey = "X"
    private lateinit var moviesAdapter: MoviesAdapter
    private val viewModel: MoviesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ShowNowLayoutBinding.inflate(inflater, container, false)
        moviesAdapter = MoviesAdapter(emptyList()) { movie ->
            showMovieDetails(movie)
        }
        binding?.ShowNowRV?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = moviesAdapter
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.movies.observe(viewLifecycleOwner, Observer {
            moviesAdapter.updateData(it)
        })
        fetchPopularMovies()
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
            movie.isLiked = !movie.isLiked
            val scaleAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_anim)
            dialogBinding.likeButton.startAnimation(scaleAnimation)
            dialogBinding.likeButton.setImageResource(if (movie.isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_empty)
        }

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.setLayout(width, height)
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
