package com.example.cinemaproject.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinemaproject.Repository.AuthRepository

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _authState = MutableLiveData<Result<Void?>>()
    val authState: LiveData<Result<Void?>> = _authState

    fun registerUser(email: String, password: String, imageUri: Uri?) {
        authRepository.registerUser(email, password, imageUri).observeForever { result ->
            _authState.value = result
        }
    }

    fun loginUser(email: String, password: String) {
        authRepository.loginUser(email, password).observeForever { result ->
            _authState.value = result
        }
    }
}

class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}