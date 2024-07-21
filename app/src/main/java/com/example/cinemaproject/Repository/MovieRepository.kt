package com.example.cinemaproject.Repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.cinemaproject.APIService.MovieDao
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.Classes.MovieDatabase

class MovieRepository(application: Application) {

    private val movieDao: MovieDao

    init {
        val database = MovieDatabase.getDatabase(application.applicationContext)
        movieDao = database.movieDao()
    }

    fun getMovies(): LiveData<List<Movie>> {
        return movieDao.getMovies()
    }
}
