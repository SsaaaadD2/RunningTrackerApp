package com.saadm.runningtracker.other

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.saadm.runningtracker.db.Run
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView (
        val runs: List<Run>,
        c : Context,
        layoutId: Int
): MarkerView(c, layoutId){

    //MPPointF is a point with X and Y vlaue
    //returns where the box should show
    //Often bars are very low, and box is cut off so we override
    //Values come from docs
    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())

    }


    //We can set the text of text views
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null){
            return
        }

        //In StasticsFragment we mapped indices to the x value
        //So now we have index of run we want
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp        //The date of run in milliseconds
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKmh}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMetres / 1000}km"
        tvDistance.text = distanceInKm

        tvDuration.text = TrackingUtility.getFormattedStopwatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurnt} calories"
        tvCaloriesBurned.text = caloriesBurned
    }

}