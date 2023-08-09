package com.denisolegovichkozlov.gps100.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.denisolegovichkozlov.gps100.R
import com.denisolegovichkozlov.gps100.databinding.ActivityMainBinding
import com.denisolegovichkozlov.gps100.databinding.FragmentMainBinding
import com.denisolegovichkozlov.gps100.databinding.SettingsBinding
import com.denisolegovichkozlov.gps100.databinding.ViewTrackBinding
import com.denisolegovichkozlov.gps100.utils.showToast


class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    private fun init() {
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPrefs(  )
    }

    private fun onChangeListener(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener() {
            pref, value ->
                when(pref.key) {
                    "update_time_key" -> onTimeChange(value.toString())
                    "color_key" -> onColorChange(pref, value.toString())

                }
            true
        }
    }

    private fun onTimeChange(value: String) {
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val pos = valueArray.indexOf(value)
        val title = timePref.title.toString().substringBefore(":")
        timePref.title = "$title: ${nameArray[pos]}"
    }

    private fun onColorChange(pref:Preference, value: String) {
        pref.icon?.setTint(Color.parseColor(value))
    }

    private fun initPrefs() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000"))
        val title = timePref.title
        timePref.title = "$title: ${nameArray[pos]}"

        val trackColor = pref?.getString("color_key", "#FF5722")
        colorPref.icon?.setTint(Color.parseColor(trackColor))

    }
}