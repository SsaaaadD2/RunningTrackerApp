package com.saadm.runningtracker.ui.fragments

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.saadm.runningtracker.R
import com.saadm.runningtracker.db.Run
import com.saadm.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.saadm.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.saadm.runningtracker.other.Constants.MAP_ZOOM
import com.saadm.runningtracker.other.Constants.POLYLINE_COLOR
import com.saadm.runningtracker.other.Constants.POLYLINE_WIDTH
import com.saadm.runningtracker.other.TrackingUtility
import com.saadm.runningtracker.services.Polyline
import com.saadm.runningtracker.services.Polylines
import com.saadm.runningtracker.services.TrackingService
import com.saadm.runningtracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null

    private var currenTimeInMillis: Long = 0L
    private var isTracking:Boolean = false
    private var pathPoints = mutableListOf<Polyline>()

    @set:Inject
    var weight = 100f

    private var menu: Menu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSave()
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }

        subscribeToObservers()

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer{
            currenTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopwatchTime(currenTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currenTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true    //We only have one menu item, so we can access it at index 0
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                cancelRunAlertDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cancelRunAlertDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Cancel Run")
                .setMessage("Are you sure you want to cancel your run? Your data will not be saved")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Yes"){ _,_ ->
                    stopRun()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.cancel()
                }
                .create()
        dialog.show()
    }


    //Cancelling or finishing the run
    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    //Finish the run and save to database, take screenshot of map
    private fun endRunAndSave(){
        map?.snapshot { bmp ->
            var distanceInMetres = 0
            for(polyline in pathPoints){
                distanceInMetres += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            //In km/h so convert m to km and convert milliseconds to hours
            //Single decimal place, so multiply by 10 to save first decimal, round it off to 0, then divide by 10
            //to get the first decimal back
            val avgSpeed = round((distanceInMetres / 1000f) / (currenTimeInMillis / 1000f / 3600) * 10) / 10f

            //date is saved in millis in database
            val dateTimeStamp = Calendar.getInstance().timeInMillis

            val caloriesBurned = ((distanceInMetres / 1000f) * weight).toInt()

            val run = Run(bmp, dateTimeStamp, avgSpeed, distanceInMetres, currenTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                    //When saving the run, we navigate back to RunFragment, so TrackingFragment has been removed
                    //So we need the root view of the activity
                    requireActivity().findViewById(R.id.rootView),
                    "Run saved successfully",
                    Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }


    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else{
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }


    //Make the camera follow the user on the map
    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            Timber.d("Here")
            map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathPoints.last().last(),
                            MAP_ZOOM
                    )
            )
        } else{
            Timber.d("Otherwise here")
        }
    }


    //When we want to see the entire track we've run
    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints){
            //For each lat-long coordinate
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05).toInt()     //padding so the track is in the middle, not on boundary
                )
        )
    }


    //In case activity was destroyed, we still have LiveData so its not a problem but we lose the drawing on the map
    //So we redraw on the map
    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polyLineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLong = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLong = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .add(preLastLatLong)
                    .add(lastLatLong)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun sendCommandToService(action: String){
		//The .also just specifies that you should do something else with the object that is created
		//Should be used as part of object construction in a chain, as it is done below
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            //Does not actually start the service each time you call this function
            //It just delivers this intent to the service
            requireContext().startService(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}