package com.ozgurbaykal.ecored

import android.app.Application
import com.ozgurbaykal.ecored.util.SharedPreferencesHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EcoRedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferencesHelper.init(this)
    }
}