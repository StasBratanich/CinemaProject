package com.example.cinemaproject.Classes

data class User(
    var email: String? = null,
    var password: String? = null
) {

    override fun toString(): String {
        return "User(email=$email, password=$password)"
    }
}
