package com.saadm.runningtracker.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    /**
     * Queries to return all values sorted by each variable
     *
    * **/
    //the table name matches the entity name of our data class
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKmh DESC")
    fun getAllRunsByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMetres DESC")
    fun getAllRunsByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsByDuration(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunsByCalories(): LiveData<List<Run>>


    /**
     * Queries to return the totals of each variable, and the total average for speed
     *
     * **/

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalRunTime(): LiveData<Long>

    @Query("SELECT SUM(distanceInMetres) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCaloriesBurnt(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKmh) FROM running_table")
    fun getTotalAverageSpeed(): LiveData<Float>
}