package com.chy.radio.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.chy.radio.domain.PlaybackError
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationKind
import kotlin.test.Test
import kotlin.test.assertEquals

// The ExoPlayer -> PlayerState rules, on plain values. ExoAudioPlayer itself needs a device:
// it is checked by hand.
class ExoStateMappingTest {
    private val fip = Station(
        id = "FIP",
        name = "FIP",
        description = null,
        streamUrl = "https://example.com/fip.mp3",
        kind = StationKind.NATIONAL,
        brandId = "FIP",
        brandName = "FIP"
    )

    private fun state(
        station: Station? = fip,
        playbackState: Int = Player.STATE_READY,
        playWhenReady: Boolean = true,
        isPlaying: Boolean = true,
        errorCode: Int? = null
    ) = playerStateOf(station, playbackState, playWhenReady, isPlaying, errorCode)

    @Test
    fun `no station is idle`() {
        assertEquals(PlayerState.Idle, state(station = null))
    }

    @Test
    fun `an error wins over everything else`() {
        assertEquals(
            PlayerState.Error(fip, PlaybackError.Network),
            state(errorCode = PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        )
    }

    @Test
    fun `connection errors are network errors and the rest is a broken stream`() {
        assertEquals(
            PlaybackError.Network,
            playbackErrorOf(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        )
        assertEquals(
            PlaybackError.Network,
            playbackErrorOf(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
        )
        assertEquals(
            PlaybackError.StreamUnavailable,
            playbackErrorOf(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)
        )
        assertEquals(
            PlaybackError.StreamUnavailable,
            playbackErrorOf(PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED)
        )
    }

    @Test
    fun `a live stream that ends is unavailable`() {
        assertEquals(
            PlayerState.Error(fip, PlaybackError.StreamUnavailable),
            state(playbackState = Player.STATE_ENDED, isPlaying = false)
        )
    }

    @Test
    fun `wanting to play without data is buffering`() {
        val expected = PlayerState.Buffering(fip)
        assertEquals(expected, state(playbackState = Player.STATE_BUFFERING, isPlaying = false))
        assertEquals(expected, state(playbackState = Player.STATE_IDLE, isPlaying = false))
    }

    @Test
    fun `ready and playing is playing`() {
        assertEquals(PlayerState.Playing(fip), state())
    }

    @Test
    fun `stopped or paused by the system is idle`() {
        // Permanent focus loss or unplugged headphones: playWhenReady becomes false.
        assertEquals(PlayerState.Idle, state(playWhenReady = false, isPlaying = false))
        assertEquals(
            PlayerState.Idle,
            state(playbackState = Player.STATE_BUFFERING, playWhenReady = false, isPlaying = false)
        )
        // Transient focus loss (a call): playWhenReady stays true, playback is suppressed.
        assertEquals(PlayerState.Idle, state(playWhenReady = true, isPlaying = false))
    }
}
