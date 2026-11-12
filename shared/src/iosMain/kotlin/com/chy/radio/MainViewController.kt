package com.chy.radio

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point, called from Swift (`ContentView.swift`). A UIViewController is the iOS
 * counterpart of an Activity's content: this one hosts the shared Compose [App].
 *
 * PascalCase on purpose: Swift calls `MainViewControllerKt.MainViewController()`, and the
 * name reads as "the main view controller" on the iOS side.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
