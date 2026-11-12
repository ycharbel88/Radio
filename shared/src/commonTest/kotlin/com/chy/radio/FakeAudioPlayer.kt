package com.chy.radio

import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import kotlinx.coroutines.flow.MutableStateFlow

// An ideal player: play and stop take effect at once. Tests set `state` directly for
// what a real player reports on its own (buffering, errors).
class FakeAudioPlayer : AudioPlayer {
    override val state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    var isReleased = false

    override fun play(station: Station) {
        state.value = PlayerState.Playing(station)
    }

    override fun stop() {
        state.value = PlayerState.Idle
    }

    override fun release() {
        isReleased = true
    }
}
