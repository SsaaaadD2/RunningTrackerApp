package com.saadm.runningtracker.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saadm.runningtracker.db.Run
import com.saadm.runningtracker.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
    ) : ViewModel() {

        fun insertRun(run: Run) = viewModelScope.launch{
            mainRepository.addRun(run)
        }
}