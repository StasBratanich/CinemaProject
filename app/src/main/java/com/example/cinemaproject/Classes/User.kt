package com.example.cinemaproject.Classes

data class User(
    var email: String? = null,
    var password: String? = null
) {
    private var likedGames: MutableMap<String, Boolean> = mutableMapOf()

    fun likeGame(gameId: String) {
        likedGames[gameId] = true
    }

    fun unlikeGame(gameId: String) {
        likedGames.remove(gameId)
    }

    fun getLikedGames(): MutableMap<String, Boolean> {
        return likedGames
    }

    fun setLikedGames(likedGames: MutableMap<String, Boolean>) {
        this.likedGames = likedGames
    }

    override fun toString(): String {
        return "User(email=$email, password=$password)"
    }
}
