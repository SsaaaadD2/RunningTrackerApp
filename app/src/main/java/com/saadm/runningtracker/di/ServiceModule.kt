package com.saadm.runningtracker.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.saadm.runningtracker.R
import com.saadm.runningtracker.other.Constants
import com.saadm.runningtracker.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)     //Only lasts as long as service, not whole application
object ServiceModule {


    @Provides
    @ServiceScoped
    fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
    ) = FusedLocationProviderClient(context)


    @Provides
    @ServiceScoped
    fun provideMainActivityPendingIntent(
            @ApplicationContext context: Context
    ) = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )


    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
            @ApplicationContext context:Context,
            pendingIntent: PendingIntent
    ) =  NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)           //Notification doesn't disappear if user clicks it
            .setOngoing(true)               //Notification cannot be swiped away
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
}