package com.example.cinemaproject.Fragments

import MovieApiService
import RetrofitInstance
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinemaproject.Adapters.MoviesAdapter
import com.example.cinemaproject.databinding.ShowNowLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowNowFragment : Fragment() {
    private var binding: ShowNowLayoutBinding? = null
    private val apiKey = "b947235f7bf13a6bcad6afa6e8e53d2d"
    private lateinit var moviesAdapter: MoviesAdapter
    private val movieApiService: MovieApiService by lazy {
        RetrofitInstance.retrofit.create(MovieApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ShowNowLayoutBinding.inflate(inflater, container, false)
        moviesAdapter = MoviesAdapter(emptyList())
        binding?.ShowNowRV?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moviesAdapter
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchPopularMovies()
    }

    private fun fetchPopularMovies() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val deferredMovies = (1..5).map { page ->
                    async { movieApiService.getPopularMovies(apiKey, page).movies }
                }
                val allMovies = deferredMovies.awaitAll().flatten()
                withContext(Dispatchers.Main) {
                    moviesAdapter.updateData(allMovies)
                }
            } catch (e: Exception) {
                // Handle the error
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}