package com.saadm.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.saadm.runningtracker.R
import com.saadm.runningtracker.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.saadm.runningtracker.other.Constants.KEY_NAME
import com.saadm.runningtracker.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPrefs : SharedPreferences

    //Boolean is a primitive type, therefore cannot use lateinit
    //And we have to use set:Inject
    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstAppOpen){
            //Default behaviour on navigation is to put current fragment on back stack
            //We want to remove this fragment from the stack
             val navOptions = NavOptions.Builder()
                     .setPopUpTo(R.id.setupFragment, true)
                     .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment, savedInstanceState, navOptions)
        }
        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPrefs()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else{
                Snackbar.make(requireView(), "Please enter all fields", Snackbar.LENGTH_SHORT).show()
            }

        }
    }


    //Returns boolean to tell if success or failure
    private fun writePersonalDataToSharedPrefs() : Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if(name.isEmpty() || weight.isEmpty()){
            return false
        }

        sharedPrefs.edit()
                .putString(KEY_NAME, name)
                //Don't need check for float because input type is decimal in view
                .putFloat(KEY_WEIGHT, weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)   //Not the first launch of app
                .apply()        //Asynchronous

        val toolbarText = "Let's go, ${name}!"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true

    }
}