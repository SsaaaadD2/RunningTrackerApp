package com.saadm.runningtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.saadm.runningtracker.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Dont need to initialise, since this dependency is injected
    //This annotation means it will search in modules (package di) for a return type of RunDAO
    @Inject
    lateinit var dao: RunDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}