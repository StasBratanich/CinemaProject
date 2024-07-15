package com.example.cinemaproject.Repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun registerUser(email: String, password: String, imageUri: Uri?): LiveData<Result<Void?>> {
        val resultLiveData = MutableLiveData<Result<Void?>>()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(imageUri, it.uid, resultLiveData)
                        } else {
                            saveUserToDatabase(it.uid, email, null, resultLiveData)
                        }
                    }
                } else {
                    resultLiveData.value = Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
        return resultLiveData
    }

    fun loginUser(email: String, password: String): LiveData<Result<Void?>> {
        val resultLiveData = MutableLiveData<Result<Void?>>()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    resultLiveData.value = Result.success(null)
                } else {
                    resultLiveData.value = Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
        return resultLiveData
    }

    private fun uploadImageToFirebaseStorage(
        imageUri: Uri, userId: String, resultLiveData: MutableLiveData<Result<Void?>>
    ) {
        val storageReference = storage.reference.child("profileImages/$userId/${UUID.randomUUID()}.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    saveUserToDatabase(userId, auth.currentUser?.email!!, uri.toString(), resultLiveData)
                }
            }
            .addOnFailureListener { exception ->
                resultLiveData.value = Result.failure(exception)
            }
    }

    private fun saveUserToDatabase(
        userId: String, email: String, imageUrl: String?, resultLiveData: MutableLiveData<Result<Void?>>
    ) {
        val user = mapOf(
            "email" to email,
            "profileImage" to imageUrl
        )

        database.getReference("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    resultLiveData.value = Result.success(null)
                } else {
                    resultLiveData.value = Result.failure(task.exception ?: Exception("Unknown error"))
                }
            }
    }
}