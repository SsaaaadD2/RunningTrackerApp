package com.saadm.runningtracker.services

import android.annotation.SuppressLint
import android.app.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
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
import com.saadm.runningtracker.other.Constants.TIMER_UPDATE_INTERVAL
import com.saadm.runningtracker.other.TrackingUtility
import com.saadm.runningtracker.ui.MainActivity
import com.saadm.runningtracker.ui.fragments.TrackingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject

//We use a list of lists, in case there is a break in tracking, we need to join the paths of coordinates
//Each path is a list of coordinates, but for several paths, there will be a list of paths
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    var isFirstRun: Boolean = true
    var serviceKilled: Boolean = false
    private var isTimerEnabled: Boolean = false
    private var lapTime: Long = 0L  //If timer is paused and resumed, we start again from 0, we don't continue
    private var totalTimeRun: Long = 0L
    private var timeStarted:Long = 0L
    private var lastSecondTimeStamp: Long = 0L


    //Dependencies come from ServiceModule
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    //To update notifications with buttons and time
    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    //This LiveData is only used by this class, so we don't need companion object
    private val timeRunInSeconds = MutableLiveData<Long>()

    //These LiveData we want to observe from outside the class, so we use companion object
    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }


    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        currentNotificationBuilder = baseNotificationBuilder

        //We can declare "this" as a lifecycle owner because we defined this service class as LifecycleService()
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }


    //Provide initial, empty values for LiveData
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }


    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
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
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
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


    //Called whenever we start or resume service
    //As opposed to startForegroundService() which is only called on start, not resume
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!){
                //Time difference between now and time started
                lapTime = System.currentTimeMillis() - timeStarted

                //post new lap time
                timeRunInMillis.postValue(totalTimeRun + lapTime)

                //If a full second has passed since the last second, update the time run in seconds
                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            //We come here once we are no longer tracking/paused i.e. the lap is complete
            totalTimeRun += lapTime
        }
    }


    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply{
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else{
            val resumeIntent = Intent(this, TrackingService::class.java).apply{
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Swap the current notification with a new one, don't just add more
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply{
            isAccessible = true
            //This clears the current notification
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        //This function might get called one more time after the notification was removed, so there would still
        //be a notification
        if(!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }


    }


    //We can suppress this warning, the warning is that we need to do a permission check
    //We do a permission check but we use EasyPermissions in our TrackingUtility,
    //so Android doesn't recognise it
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show()

        }
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
                        Timber.d("adding location")
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
            Timber.d("Location not null")
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply{
                last().add(pos)
                pathPoints.postValue(this)
            }
        } ?: Timber.d("Location is null")

    }


    //Only called on start, not resume
    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notifManager)
        }

        //This observer might get called one more time after the notification was removed, so there would still
        //be a notification
        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled){
                val notification = currentNotificationBuilder
                        .setContentText(TrackingUtility.getFormattedStopwatchTime(it * 1000L))
                notifManager.notify(NOTIFICATION_ID, notification.build())
            }

        })
        //System function
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
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
}