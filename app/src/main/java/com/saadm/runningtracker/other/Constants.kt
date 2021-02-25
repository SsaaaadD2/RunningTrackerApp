package com.saadm.runningtracker.other

import android.graphics.Color

object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val LOCATION_PERMISSION_REQUEST_CODE = 42

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 5000L  //in milliseconds
    const val FASTEST_LOCATION_INTERVAL = 2000L     //minimum interval

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val NOTIFICATION_CHANNEL_NAME = "Tracking App"
    const val NOTIFICATION_CHANNEL_ID= "running_tracker"
    const val NOTIFICATION_ID= 30
}