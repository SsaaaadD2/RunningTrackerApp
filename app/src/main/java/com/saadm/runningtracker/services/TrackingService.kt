package com.saadm.runningtracker.services

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.saadm.runningtracker.R
import com.saadm.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.saadm.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.saadm.runningtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.saadm.runningtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.saadm.runningtracker.other.Constants.NOTIFICATION_ID
import com.saadm.runningtracker.ui.MainActivity
import timber.log.Timber

class TrackingService: LifecycleService() {

    var isFirstRun: Boolean = true


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

    private fun startForegroundService(){
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