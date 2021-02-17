package com.saadm.runningtracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="running_table")
data class Run (
    var img: Bitmap? = null,
    //use Long instead of Date because Longs are easier to sort
    //timestamp is saving millisecods
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var distanceInMetres: Int = 0,
    //Not to be confused with timestamp: timestamp is when the run was, this is how long it was
    var timeInMillis: Long = 0L,
    var caloriesBurnt: Int = 0
    ){
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
}