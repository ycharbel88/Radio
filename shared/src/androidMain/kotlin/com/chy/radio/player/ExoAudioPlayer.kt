package com.chy.radio.player

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Android [AudioPlayer]: ExoPlayer, foreground only (no service, no notification).
 *
 * Foreground only: it stops when [ProcessLifecycleOwner] stops, not when the Activity does.
 * An Activity stops on every rotation (it is destroyed and recreated), which would cut the
 * sound. The process lifecycle only stops once no activity has been visible for 700 ms, so
 * a rotation keeps playing while leaving the app stops it.
 *
 * Main thread only (ExoPlayer's rule: one thread, the one that built it). Our callers keep
 * to it: Koin builds this player inside `koinViewModel()`, during composition; the ViewModel
 * calls [play] and [stop] from click handlers and [release] from `onCleared()`; ExoPlayer's
 * listener and [ProcessLifecycleOwner] call back on the main thread too.
 *
 * @param context only its application context is kept, so no Activity leaks.
 */
class ExoAudioPlayer(context: Context) : AudioPlayer {
    private val exoPlayer = ExoPlayer
        .Builder(context.applicationContext)
        // handleAudioFocus = true: ExoPlayer requests audio focus on play and reacts to its
        // loss on its own: silent during a call and resumes after it, stops for another
        // player, lowers the volume for a notification.
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        // Unplugged headphones: stop instead of suddenly playing on the speaker.
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val mutableState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private var station: Station? = null

    // Every change of the player (state, error, isPlaying) arrives here, batched by onEvents.
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish()
    }

    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) = stop()
    }

    init {
        exoPlayer.addListener(playerListener)
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    override fun play(station: Station) {
        this.station = station
        mutableState.value = PlayerState.Buffering(station)
        // Always a new item: every play joins the live stream.
        val mediaItem = MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(station.streamUrl)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun stop() {
        // Forget the station first, so the events that stop() triggers map to Idle.
        station = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        mutableState.value = PlayerState.Idle
    }

    override fun release() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    private fun publish() {
        val current = station
        // Ignore events about an item that is no longer ours (stopped or replaced).
        val isCurrentItem = current != null && current.id == exoPlayer.currentMediaItem?.mediaId
        mutableState.value = playerStateOf(
            station = if (isCurrentItem) current else null,
            playbackState = exoPlayer.playbackState,
            playWhenReady = exoPlayer.playWhenReady,
            isPlaying = exoPlayer.isPlaying,
            errorCode = exoPlayer.playerError?.errorCode
        )
    }
}
