package com.voiddrift.game

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoidDriftApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
