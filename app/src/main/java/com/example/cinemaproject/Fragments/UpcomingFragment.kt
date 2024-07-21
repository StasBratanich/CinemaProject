package com.example.cinemaproject.Fragments
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cinemaproject.databinding.UpcomingLayoutBinding


class UpcomingFragment : BaseMovieFragment() {
    private var _binding: UpcomingLayoutBinding? = null
    private val binding get() = _binding!!

    override val recyclerView: RecyclerView
        get() = binding.upcomingRecycleView

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = UpcomingLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun observeMovies() {
        viewModel.upcomingMovies.observe(viewLifecycleOwner) { movies ->
            moviesAdapter.updateData(movies)
            updateMovieLikedStates()
        }
    }

    override fun fetchMovies() {
        viewModel.fetchUpcomingMovies(apiKey)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
