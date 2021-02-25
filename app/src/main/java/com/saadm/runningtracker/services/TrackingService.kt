package com.saadm.runningtracker.services

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.saadm.runningtracker.R
import com.saadm.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.saadm.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.saadm.runningtracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.saadm.runningtracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.saadm.runningtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.saadm.runningtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.saadm.runningtracker.other.Constants.NOTIFICATION_ID
import com.saadm.runningtracker.other.TrackingUtility
import com.saadm.runningtracker.ui.MainActivity
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService: LifecycleService() {

    var isFirstRun: Boolean = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    //Provide initial, empty values for LiveData
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        //We can declare "this" as a lifecycle owner because we defined this service class as LifecycleService()
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    //Called when an Intent is sent to this service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		//.let is usually used for value conversions
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //If value is not null, it will do the apply thing
    //If it is null, it will do the stuff after the question mark
    private fun addEmptyPolyline() = pathPoints.value?.apply{
        //it adds this empty list to the end of the pathPoints value
        add(mutableListOf())
        pathPoints.postValue(this)
    //The question mark here is what to do if the value was null
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    //We can suppress this warning, the warning is that we need to do a permission check
    //We do a permission check but we use EasyPermissions in our TrackingUtility,
    //so Android doesn't recognise it
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply{
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                )
            }
        } else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    val locationCallback = object: LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            //isTracking will not be null, so no need for null check
            if(isTracking.value!!){
                result?.locations?.let{ locations ->
                    for(location in locations){
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?){
        //We use let and apply here because location is nullable, so it only does the things inside the let
        //if it is not null
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply{
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }


    private fun startForegroundService(){
        addEmptyPolyline()
        isTracking.postValue(true)
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notifManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)           //Notification doesn't disappear if user clicks it
                .setOngoing(true)               //Notification cannot be swiped away
                .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                .setContentTitle("Running App")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notifManager: NotificationManager){
        val notifChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
        )
        notifManager.createNotificationChannel(notifChannel)
    }

    private fun getMainActivityPendingIntent() : PendingIntent {
        return PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).also {
                    it.action = ACTION_SHOW_TRACKING_FRAGMENT
                },
                FLAG_UPDATE_CURRENT
        )
    }
}