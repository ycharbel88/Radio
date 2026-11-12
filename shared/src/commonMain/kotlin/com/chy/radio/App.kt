package com.chy.radio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chy.radio.domain.AudioPlayer
import com.chy.radio.sample.SampleStationRepository
import com.chy.radio.ui.StationsScreen
import com.chy.radio.ui.StationsViewModel

/**
 * Composition root: the only place that knows both `data` and `ui`.
 * A factory, not a player: `viewModel { }` runs it once, so a rotation builds no second player.
 */
@Composable
fun App(playerFactory: () -> AudioPlayer) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        Surface(Modifier.fillMaxSize()) {
            val viewModel =
                viewModel { StationsViewModel(SampleStationRepository(), playerFactory()) }
            StationsScreen(viewModel)
        }
    }
}
