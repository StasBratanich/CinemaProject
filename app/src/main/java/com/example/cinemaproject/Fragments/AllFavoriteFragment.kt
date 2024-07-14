package com.example.cinemaproject.Fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cinemaproject.databinding.FavoriteLayoutBinding

class AllFavoriteFragment : Fragment() {
    private var _binding : FavoriteLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FavoriteLayoutBinding.inflate(inflater, container, false)
        return _binding?.root
    }
}