package com.example.cinemaproject.Fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cinemaproject.R
import com.example.cinemaproject.Repository.AuthRepository
import com.example.cinemaproject.ViewModel.AuthViewModel
import com.example.cinemaproject.ViewModel.AuthViewModelFactory
import com.example.cinemaproject.databinding.LoginLayoutBinding

class LoginFragment : Fragment() {

    private var _binding : LoginLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginLayoutBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authRepository = AuthRepository()
        val authViewModelFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authViewModelFactory).get(AuthViewModel::class.java)

        authViewModel.authState.observe(viewLifecycleOwner, Observer { result ->
            result.fold(
                onSuccess = {
                    findNavController().navigate(R.id.action_loginFragment_to_welcomeFragment)
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Authentication failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        })

        binding.LoginBTN.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.loginUser(email, password)
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
