package com.denisolegovichkozlov.gps100.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>
): Serializable
