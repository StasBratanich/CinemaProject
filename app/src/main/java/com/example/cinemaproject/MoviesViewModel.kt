package com.example.cinemaproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.Repository.MovieRepository

class MoviesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovieRepository(application)
    val movies: LiveData<List<Movie>>? = repository.getMovies()

    fun addMovie(movie: Movie) {
        repository.addMovie(movie)
    }

    fun deleteMovie(movie: Movie) {
        repository.deleteMovie(movie)
    }
}
