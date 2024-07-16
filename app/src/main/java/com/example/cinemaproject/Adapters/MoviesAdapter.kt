package com.example.cinemaproject.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemaproject.Classes.Movie
import com.example.cinemaproject.databinding.RvMovieBinding

class MoviesAdapter(private var movies: List<Movie>, private val onItemClick: (Movie) -> Unit) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = RvMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            onItemClick(movie)
        }
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    fun updateData(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }

    inner class MovieViewHolder(private val binding: RvMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.RvMovieName.text = movie.title
            movie.posterPath?.let {
                val imageUrl = "https://image.tmdb.org/t/p/w500$it"
                Glide.with(binding.root)
                    .load(imageUrl)
                    .into(binding.RvMovieImg)
            }
        }
    }
}
