package com.chy.radio.sample

import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TEMPORARY, deleted in step 4 (replaced by ExoPlayer on Android and AVPlayer on iOS).
 *
 * Plays no sound: it only reports Playing or Idle, so the screen can be tried on both
 * platforms before the native players exist.
 */
class SimulatedAudioPlayer : AudioPlayer {
    private val mutableState = MutableStateFlow<PlayerState>(PlayerState.Idle)

    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    override fun play(station: Station) {
        mutableState.value = PlayerState.Playing(station)
    }

    override fun stop() {
        mutableState.value = PlayerState.Idle
    }

    override fun release() {
        // Nothing to free: this player holds no native resource.
    }
}
