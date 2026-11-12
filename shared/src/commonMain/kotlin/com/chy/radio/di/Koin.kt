package com.chy.radio.di

import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.StationRepository
import com.chy.radio.sample.SampleStationRepository
import com.chy.radio.ui.StationsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The composition root: the only place that knows both the data side and the UI side.
 * Each platform passes its own player through [playerFactory].
 */
fun appModule(playerFactory: () -> AudioPlayer) = module {
    single<StationRepository> { SampleStationRepository() }

    // A factory, never a single. Each ViewModel releases its own player in onCleared(), so
    // a single would hand a released player to the next ViewModel (back out of the app,
    // then reopen it while the process is still alive).
    factory<AudioPlayer> { playerFactory() }

    viewModel { StationsViewModel(repository = get(), player = get()) }
}

/** Starts Koin. Call it once per process, from each platform's entry point. */
fun initKoin(playerFactory: () -> AudioPlayer) {
    startKoin { modules(appModule(playerFactory)) }
}
