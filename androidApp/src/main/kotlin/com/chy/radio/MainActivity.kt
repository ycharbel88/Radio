package com.chy.radio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chy.radio.sample.SimulatedAudioPlayer

/** Android entry point: shows the shared [App]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(playerFactory = { SimulatedAudioPlayer() })
        }
    }
}
