package com.example.cinemaproject.Fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cinemaproject.R
import com.example.cinemaproject.databinding.WelcomeLayoutBinding

class WelcomeFragment : Fragment() {

    private var _binding: WelcomeLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WelcomeLayoutBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ShowNowBtn.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_showNowFragment)
        }

        binding.UpcomingBtn.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_upcomingFragment)
        }

        binding.FavoriteBtn.setOnClickListener{
            findNavController().navigate(R.id.action_welcomeFragment_to_allFavoriteFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}