package com.example.gyromouse

import android.app.Application
import android.util.Log

class GyroMouseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("GyroMouseApp", "Uncaught exception", throwable)
        }
    }
}