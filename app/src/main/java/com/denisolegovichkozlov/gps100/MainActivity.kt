package com.denisolegovichkozlov.gps100

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.denisolegovichkozlov.gps100.databinding.ActivityMainBinding
import com.denisolegovichkozlov.gps100.fragments.MainFragment
import com.denisolegovichkozlov.gps100.fragments.SettingsFragment
import com.denisolegovichkozlov.gps100.fragments.TracksFragment
import com.denisolegovichkozlov.gps100.utils.openFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBottomNavClicks()
        openFragment(MainFragment.newInstance() )
    }

    private fun onBottomNavClicks(){
        binding.bNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> openFragment(MainFragment.newInstance())
                R.id.routes -> openFragment(TracksFragment.newInstance())
                R.id.settings -> openFragment(SettingsFragment())
            }
            true
        }
    }
}