package com.saadm.runningtracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saadm.runningtracker.BaseApplication
import com.saadm.runningtracker.db.RunningDatabase
import com.saadm.runningtracker.other.Constants.RUNNING_DATABASE_NAME
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
    fun provideRunningDAO(db: RunningDatabase){
        db.getRunDAO()
    }
}