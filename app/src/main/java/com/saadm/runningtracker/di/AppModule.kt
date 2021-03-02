package com.saadm.runningtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saadm.runningtracker.BaseApplication
import com.saadm.runningtracker.db.RunningDatabase
import com.saadm.runningtracker.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.saadm.runningtracker.other.Constants.KEY_NAME
import com.saadm.runningtracker.other.Constants.KEY_WEIGHT
import com.saadm.runningtracker.other.Constants.RUNNING_DATABASE_NAME
import com.saadm.runningtracker.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(@ApplicationContext app : Context) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideRunningDAO(db: RunningDatabase) = db.getRunDAO()


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext app: Context) =
            //MODE_PRIVATE means only our app is allowed to read from these sharedprefs
            app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)


    @Provides
    @Singleton
    fun providesName(sharedPrefs: SharedPreferences) =
            //Even though if KEY_NAME can't be found an empty string should be defaulted
            //In kotlin sometimes null is returned, so we need to do the null check
            sharedPrefs.getString(KEY_NAME, "") ?: ""

    @Provides
    @Singleton
    fun providesWeight(sharedPrefs: SharedPreferences) =
            //Unlike getString, we don't need null check here
            sharedPrefs.getFloat(KEY_WEIGHT, 80f)


    @Provides
    @Singleton
    fun providesFirstTimeToggle(sharedPrefs: SharedPreferences) =
            //Unlike getString, we don't need null check here
            sharedPrefs.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}