package com.saadm.runningtracker.db

import androidx.room.Database
import androidx.room.TypeConverters

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converter::class)
abstract class RunningDatabase {
    
    abstract fun getRunDAO() : RunDAO

}