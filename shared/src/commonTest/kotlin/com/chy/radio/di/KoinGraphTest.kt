package com.chy.radio.di

import com.chy.radio.FakeAudioPlayer
import com.chy.radio.domain.AudioPlayer
import com.chy.radio.ui.StationsViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication

// koinApplication, not startKoin: an isolated graph per test, no global Koin to stop.
@OptIn(ExperimentalCoroutinesApi::class)
class KoinGraphTest {
    private val koinApplication = koinApplication {
        modules(appModule(playerFactory = { FakeAudioPlayer() }))
    }

    // The ViewModel starts loading on Dispatchers.Main as soon as it is created.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `the graph builds the stations view model`() {
        val viewModel = koinApplication.koin.get<StationsViewModel>()

        assertNotNull(viewModel)
    }

    @Test
    fun `each request for a player gets a new one`() {
        val firstPlayer = koinApplication.koin.get<AudioPlayer>()
        val secondPlayer = koinApplication.koin.get<AudioPlayer>()

        assertNotSame(firstPlayer, secondPlayer)
    }
}
