package com.example.cinemaproject.Repository

import MovieDatabase
import android.app.Application
import com.example.cinemaproject.APIService.MovieDao
import com.example.cinemaproject.Classes.Movie

class MovieRepository(application: Application) {

    private val movieDao: MovieDao?

    init {
        val database = MovieDatabase.getDatabase(application.applicationContext)
        movieDao = database?.movieDao()
    }

    fun getMovies() = movieDao?.getMovies()

    fun addMovie(movie:Movie){
        movieDao?.addMovie(movie)
    }

    fun deleteMovie(movie:Movie){
        movieDao?.deleteMovie(movie)
    }

}
