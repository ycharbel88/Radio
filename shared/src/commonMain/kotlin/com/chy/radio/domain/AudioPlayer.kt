package com.chy.radio.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Plays one live station at a time. The UI only ever sees this interface; each
 * platform entry point passes its own implementation to `App`.
 *
 * Commands are fire-and-forget (not `suspend`): platform players are asynchronous
 * and report what actually happens through [state].
 *
 * Contract:
 * - [play] always joins the live stream and replaces whatever was playing.
 * - [stop] stops and forgets the station.
 * - [release] frees native resources. The player must not be used after it.
 */
interface AudioPlayer {
    val state: StateFlow<PlayerState>

    fun play(station: Station)

    fun stop()

    fun release()
}

/** The player as the station list sees it. */
sealed interface PlayerState {
    data object Idle : PlayerState

    data class Buffering(val station: Station) : PlayerState

    data class Playing(val station: Station) : PlayerState

    data class Error(val station: Station, val reason: PlaybackError) : PlayerState
}

/** The station this state is about, or null when [PlayerState.Idle]. */
val PlayerState.station: Station?
    get() = when (this) {
        PlayerState.Idle -> null
        is PlayerState.Buffering -> station
        is PlayerState.Playing -> station
        is PlayerState.Error -> station
    }

/** Why playback failed. Typed so the UI owns the wording. */
enum class PlaybackError {
    Network,
    StreamUnavailable
}
