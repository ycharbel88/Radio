package com.chy.radio

import androidx.compose.ui.window.ComposeUIViewController
import com.chy.radio.di.initKoin
import com.chy.radio.sample.SimulatedAudioPlayer
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * iOS entry point, called from Swift (`ContentView.swift`). A UIViewController is the iOS
 * counterpart of an Activity's content: this one hosts the shared Compose [App].
 *
 * PascalCase on purpose: Swift calls `MainViewControllerKt.MainViewController()`, and the
 * name reads as "the main view controller" on the iOS side.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    // iOS has no Application class like Android. This is the first Kotlin code Swift calls,
    // so DI starts here. SwiftUI can create the controller more than once, and startKoin
    // throws if Koin is already started.
    if (KoinPlatform.getKoinOrNull() == null) {
        initKoin(playerFactory = { SimulatedAudioPlayer() })
    }
    return ComposeUIViewController { App() }
}
