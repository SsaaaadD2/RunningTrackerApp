package com.saadm.runningtracker.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saadm.runningtracker.db.Run
import com.saadm.runningtracker.other.SortType
import com.saadm.runningtracker.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
    ) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCalories = mainRepository.getAllRunsSortedByCalories()
    private val runsSortedByTime = mainRepository.getAllRunsSortedByDuration()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedBySpeed()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE

    init {
        //Lambda function called whenever there is a change in runsSortedByDate livedata
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE){
                result?.let{
                    //If sorttype is Date, then the value of runs gets set to the LiveData emitted by
                    //runsSortedByDate
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByCalories) { result ->
            if(sortType == SortType.CALORIES){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByTime) { result ->
            if(sortType == SortType.RUNNING_TIME){
                result?.let{
                    runs.value = it
                }
            }
        }

        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED){
                result?.let{
                    runs.value = it
                }
            }
        }
    }


    //In the init block, it does not update when sortType updates, therefore we need this
    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE -> runsSortedByDate.value?.let {
            runs.value = it
        }
        SortType.RUNNING_TIME -> runsSortedByTime.value?.let {
            runs.value = it
        }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let {
            runs.value = it
        }
        SortType.CALORIES-> runsSortedByCalories.value?.let {
            runs.value = it
        }
        SortType.DISTANCE -> runsSortedByDistance.value?.let {
            runs.value = it
        }
    }.also {
        this.sortType = sortType
    }


    fun insertRun(run: Run) = viewModelScope.launch{
        mainRepository.addRun(run)
    }
}