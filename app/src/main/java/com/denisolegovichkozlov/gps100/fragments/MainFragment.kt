package com.denisolegovichkozlov.gps100.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.GnssAntennaInfo.Listener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.denisolegovichkozlov.gps100.MainViewModel
import com.denisolegovichkozlov.gps100.R
import com.denisolegovichkozlov.gps100.databinding.FragmentMainBinding
import com.denisolegovichkozlov.gps100.location.LocationModel
import com.denisolegovichkozlov.gps100.location.LocationService
import com.denisolegovichkozlov.gps100.utils.DialogManager
import com.denisolegovichkozlov.gps100.utils.TimeUtils
import com.denisolegovichkozlov.gps100.utils.checkPermission
import com.denisolegovichkozlov.gps100.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.Distance
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


class MainFragment : Fragment() {
    private var pl: Polyline? = null
    private var isServiceRunning = false
    private var firstStart = true
    private var timer: Timer? = null
    private var startTime = 0L
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        Log.d("MyLog", "onCreateView")
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MyLog", "onViewCreated")
        registerPermissions()
        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
    }

    private fun startLocService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }

    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when(it.id){
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun locationUpdates() = with(binding) {
        model.locationUpdates.observe(viewLifecycleOwner) {
            val distance = "Distance:${String.format("%.1f", it.distance)} m"
            val speed = "Speed:${String.format("%.1f", 3.6 * it.velocity)} km/h"
            val averageSpeed = "Average speed: ${getAvarageSpeed(it.distance)} km/h"
            tvDistance.text = distance
            tvVelocity.text = speed
            tvAvarageVelocity.text = averageSpeed
            updatePolyline(it.geoPointsList)
        }
    }

    private fun updateTime() {
        model.timeData.observe(viewLifecycleOwner) {
            binding.tvTime.text = it
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread{
                    model.timeData.value = getCurrentTime()
                }
            }
        },
        1,
        1)
    }

    private fun getAvarageSpeed(distance: Float): String {
        return String.format("%.1f", (distance/((System.currentTimeMillis() - startTime)/1000f) * 3.6f))
    }

    private fun getCurrentTime(): String {
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
        }
        isServiceRunning = !isServiceRunning
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyLog", "onResume")
        checkLocPermission()
    }


    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }


    private fun initOSM() = with(binding) {
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix() {
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(pl)
        }
    }

    private fun registerPermissions(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                checkLocationEnabled()
                initOSM()
            } else {
                showToast("Вы не дали разрешения на использование местоположения!")
            }
        }
    }

    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            checkPermissionLocAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionLocAfter10() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            checkLocationEnabled()
            initOSM()
        } else {
            pLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            pLauncher.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }


    private fun checkPermissionBefore10() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkLocationEnabled()
            initOSM()
        } else {
            pLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) {
            DialogManager.showLocEnabledDialog(
                activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                }
                )
        } else {
            showToast("Location enabled")
        }
    }

    private val receiver =  object :BroadcastReceiver() {
        override fun onReceive(p0: Context?, i: Intent?) {
            if (i?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel = i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                Log.d("MyLog", "Distance: ${locModel.distance}")
                model.locationUpdates.value = locModel
            }
        }
    }

    private fun registerLocReceiver() {
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, locFilter)
    }

    private fun addPoints(list: List<GeoPoint>) {
        pl?.addPoint(list[list.size -1])
    }

    private fun fillPolyline(list: List<GeoPoint>) {
        list.forEach {
            pl?.addPoint(it)
        }
    }

    private fun updatePolyline(list: List<GeoPoint>) {
        if (list.size > 1 && firstStart) {
            fillPolyline(list)
            firstStart = false
        } else {
            addPoints(list)
        }
    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }
            companion object {
        @JvmStatic
        fun newInstance() =
            MainFragment()
    }
}