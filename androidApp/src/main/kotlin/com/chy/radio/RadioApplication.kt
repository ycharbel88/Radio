package com.chy.radio

import android.app.Application
import com.chy.radio.di.initKoin
import com.chy.radio.player.ExoAudioPlayer

/** Koin starts here, once per process, before any Activity. */
class RadioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(playerFactory = { ExoAudioPlayer(applicationContext) })
    }
}
