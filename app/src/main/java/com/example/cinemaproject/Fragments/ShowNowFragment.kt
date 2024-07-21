package com.example.cinemaproject.Fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemaproject.databinding.ShowNowLayoutBinding

class ShowNowFragment : BaseMovieFragment() {
    private var _binding: ShowNowLayoutBinding? = null
    private val binding get() = _binding!!

    override val recyclerView: RecyclerView
        get() = binding.ShowNowRV

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = ShowNowLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun observeMovies() {
        viewModel.popularMovies.observe(viewLifecycleOwner) { movies ->
            moviesAdapter.updateData(movies)
            updateMovieLikedStates()
        }
    }

    override fun fetchMovies() {
        viewModel.fetchPopularMovies(apiKey)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

