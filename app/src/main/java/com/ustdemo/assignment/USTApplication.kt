package com.ustdemo.assignment

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class USTApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
