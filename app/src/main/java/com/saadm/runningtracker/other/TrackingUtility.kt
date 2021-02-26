package com.saadm.runningtracker.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    //This function just checks if we have permissions, does not request them
    //That is done in the fragments
    fun hasLocationPermissions(ct: Context): Boolean{
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            return EasyPermissions.hasPermissions(
                    ct,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else{
            return EasyPermissions.hasPermissions(
                    ct,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }


    fun getFormattedStopwatchTime(ms:Long, includeMillis: Boolean = false): String{
        //Make a copy because ms passed in will most likely be "val"
        var millisCopy = ms

        val hours = TimeUnit.MILLISECONDS.toHours(millisCopy)
        millisCopy -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisCopy)
        millisCopy -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisCopy)

        //Return here if we don't want milliseconds
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}${hours}:" +
                    "${if(minutes<10) "0" else ""}${minutes}:" +
                    "${if(seconds<10) "0" else ""}${seconds}}"
        }

        millisCopy -= TimeUnit.SECONDS.toMillis(seconds)
        millisCopy /= 10        //Want 2-digits for milliseconds, not 3-digit
        return "${if(hours < 10) "0" else ""}${hours}:" +
                "${if(minutes<10) "0" else ""}${minutes}:" +
                "${if(seconds<10) "0" else ""}${seconds}:" +
                "${if (millisCopy<10) "0" else ""}${millisCopy}"

    }
}