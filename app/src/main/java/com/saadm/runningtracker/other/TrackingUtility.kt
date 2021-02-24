package com.saadm.runningtracker.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions

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
}