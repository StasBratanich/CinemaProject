package com.example.cinemaproject.Classes

class User {
    var email : String? = null
    var password : String? = null
    var profileImage : String? = null
    private var likedGames : MutableMap<String, Boolean> = mutableMapOf()

    constructor(email: String?, imageUrl: String?) {
        this.email = email
        this.profileImage = imageUrl
        this.likedGames = mutableMapOf()
    }

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
        return "UsersData(email=$email, password=$password, profileImage=$profileImage)"
    }
}