package com.example.cinemaproject.ViewModel

import MovieApiService
import RetrofitInstance
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.Repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MoviesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovieRepository(application)
    val movies: LiveData<List<Movie>> = repository.getMovies()

    private val _popularMovies = MutableLiveData<List<Movie>>()
    val popularMovies: LiveData<List<Movie>> get() = _popularMovies

    private val _upcomingMovies = MutableLiveData<List<Movie>>()
    val upcomingMovies: LiveData<List<Movie>> get() = _upcomingMovies

    fun addMovie(movie: Movie) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMovie(movie)
        }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMovie(movie)
        }
    }

    fun fetchPopularMovies(apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deferredMovies = (1..5).map { page ->
                    async { RetrofitInstance.retrofit.create(MovieApiService::class.java).getPopularMovies(apiKey, page).movies }
                }
                val allMovies = deferredMovies.awaitAll().flatten()
                _popularMovies.postValue(allMovies)
                Log.d("MoviesViewModel", "Fetched ${allMovies.size} popular movies")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MoviesViewModel", "Error fetching popular movies: ${e.message}")
            }
        }
    }

    fun fetchUpcomingMovies(apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deferredMovies = (1..5).map { page ->
                    async { RetrofitInstance.retrofit.create(MovieApiService::class.java).getUpcomingMovies(apiKey, page).movies }
                }
                val allMovies = deferredMovies.awaitAll().flatten()
                _upcomingMovies.postValue(allMovies)
                Log.d("MoviesViewModel", "Fetched ${allMovies.size} upcoming movies")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MoviesViewModel", "Error fetching upcoming movies: ${e.message}")
            }
        }
    }
}
