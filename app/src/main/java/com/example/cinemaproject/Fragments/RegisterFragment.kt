package com.example.cinemaproject.Fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cinemaproject.R
import com.example.cinemaproject.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class RegisterFragment : Fragment() {
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    private lateinit var cameraProfileImage: ActivityResultLauncher<Void?>
    private lateinit var pickImageFromGallery: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        cameraProfileImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = getImageUriFromBitmap(it)
                selectedImageUri = uri

                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.registerUploadImage)
            }
        }

        pickImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.registerUploadImage.setImageURI(it)

                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.registerUploadImage)
            }
        }

        binding.registerUploadImage.setOnClickListener {
            showImageSourceSelectionDialog()
        }

        binding.registerRegisterBTN.setOnClickListener {
            val email = binding.registerEmail.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()
            val confirmPassword = binding.registerPasswordConfirm.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (confirmPassword == password) {
                    if (selectedImageUri != null) {
                        registerUser(email, password)
                    } else {
                        Toast.makeText(context, "Please upload a profile image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageSourceSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePhoto()
                1 -> chooseFromGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun takePhoto() {
        cameraProfileImage.launch(null)
    }

    private fun chooseFromGallery() {
        pickImageFromGallery.launch("image/*")
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        selectedImageUri?.let { uri ->
                            uploadImageToFirebaseStorage(uri, it.uid)
                        } ?: run {
                            saveUserToDatabase(it.uid, email, null)
                        }
                    }
                } else {
                    Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String) {
        val storageReference = FirebaseStorage.getInstance().reference
            .child("profileImages/$userId/${UUID.randomUUID()}.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    saveUserToDatabase(userId, auth.currentUser?.email!!, uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToDatabase(userId: String, email: String, imageUrl: String?) {
        val user = mapOf(
            "email" to email,
            "profileImage" to imageUrl
        )

        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
                } else {
                    Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}