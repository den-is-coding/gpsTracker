package com.denisolegovichkozlov.gps100

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denisolegovichkozlov.gps100.location.LocationModel

class MainViewModel: ViewModel() {
    val locationUpdates = MutableLiveData<LocationModel>()
    val timeData = MutableLiveData<String>()
}