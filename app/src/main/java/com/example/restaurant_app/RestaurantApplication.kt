package com.example.restaurant_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RestaurantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}