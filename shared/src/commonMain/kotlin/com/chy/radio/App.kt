package com.chy.radio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chy.radio.ui.StationsScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * The whole app: the theme and the stations screen. Koin must be started first
 * (see `initKoin`); `koinViewModel()` keeps the ViewModel across rotations.
 */
@Composable
fun App() {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        Surface(Modifier.fillMaxSize()) {
            StationsScreen(viewModel = koinViewModel())
        }
    }
}
