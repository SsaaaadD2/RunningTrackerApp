package com.saadm.runningtracker.repositories

import com.saadm.runningtracker.db.Run
import com.saadm.runningtracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(val runDao : RunDAO) {

    suspend fun addRun(run : Run) = runDao.insertRun(run)
    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsByDate()
    fun getAllRunsSortedBySpeed() = runDao.getAllRunsByAvgSpeed()
    fun getAllRunsSortedByCalories() = runDao.getAllRunsByCalories()
    fun getAllRunsSortedByDistance() = runDao.getAllRunsByDistance()
    fun getAllRunsSortedByDuration() = runDao.getAllRunsByDuration()

    fun getTotalAverageSpeed() = runDao.getTotalAverageSpeed()
    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalDuration() = runDao.getTotalRunTime()
    fun getTotalCaloriesBurnt() = runDao.getTotalCaloriesBurnt()

}