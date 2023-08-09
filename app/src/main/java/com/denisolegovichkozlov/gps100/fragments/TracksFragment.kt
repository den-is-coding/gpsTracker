package com.denisolegovichkozlov.gps100.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.denisolegovichkozlov.gps100.databinding.TracksBinding


class TracksFragment : Fragment() {

    private lateinit var binding: TracksBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TracksFragment()
    }
}