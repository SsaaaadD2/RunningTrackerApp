package com.saadm.runningtracker.ui

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.saadm.runningtracker.R
import com.saadm.runningtracker.db.RunDAO
import com.saadm.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Dont need to initialise, since this dependency is injected
    //This annotation means it will search in modules (package di) for a return type of RunDAO
    //Done for testing purposes, dont need
    //@Inject
    //lateinit var dao: RunDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        //In case MainActivity was destroyed and needs to be created again, and we need redirection
        //The intent in the parameter comes from getIntent()
        navigateToTrackingFragmentIfNeeded(intent)

        //Only want bottomNaigationView in 3 fragments, not all
        //The underscores around destination are other parameters we dont need
        navHostFragment.findNavController().addOnDestinationChangedListener{
            _, destination, _ ->
            when(destination.id){
                R.id.settingsFragment, R.id.statisticsFragment, R.id.runFragment ->
                    bottomNavigationView.visibility = View.VISIBLE
                else -> bottomNavigationView.visibility  = View.GONE
            }
        }
    }


    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }
}