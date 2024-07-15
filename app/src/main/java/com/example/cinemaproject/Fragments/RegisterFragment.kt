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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cinemaproject.R
import com.example.cinemaproject.Repository.AuthRepository
import com.example.cinemaproject.ViewModel.AuthViewModel
import com.example.cinemaproject.ViewModel.AuthViewModelFactory
import com.example.cinemaproject.databinding.FragmentRegisterBinding
import java.io.ByteArrayOutputStream


class RegisterFragment : Fragment() {
    private var _binding : FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
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

        val authRepository = AuthRepository()
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authViewModelFactory).get(AuthViewModel::class.java)

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
                        authViewModel.registerUser(email, password, selectedImageUri)
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

        authViewModel.authState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                findNavController().navigate(R.id.action_registerFragment_to_welcomeFragment)
            }
            result.onFailure { exception ->
                Toast.makeText(context, "${exception.message}", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}