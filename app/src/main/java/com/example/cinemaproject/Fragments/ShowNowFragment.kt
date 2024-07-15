package com.example.cinemaproject.Fragments

import MovieApiService
import RetrofitInstance
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cinemaproject.Adapters.MoviesAdapter
import com.example.cinemaproject.ViewModel.MoviesViewModel
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
    private val viewModel: MoviesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ShowNowLayoutBinding.inflate(inflater, container, false)
        moviesAdapter = MoviesAdapter(emptyList())
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
