package com.chy.radio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.DataResult
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationRepository
import com.chy.radio.domain.station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Owns the player and releases it in [onCleared]. */
class StationsViewModel(
    private val repository: StationRepository,
    private val player: AudioPlayer
) : ViewModel() {
    private val content = MutableStateFlow<StationsContent>(StationsContent.Loading)

    // Eagerly: the ViewModel lives exactly as long as the screen, so no need to stop early.
    val uiState: StateFlow<StationsUiState> =
        combine(content, player.state) { content, playerState ->
            StationsUiState(content, playerState)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = StationsUiState(content.value, player.state.value)
        )

    init {
        load()
    }

    // Ignored while Loading: a request is already in flight, so never two at once.
    fun onRetry() {
        val current = content.value
        if (current !is StationsContent.Error && current != StationsContent.Empty) return
        content.value = StationsContent.Loading
        load()
    }

    fun onStationClick(station: Station) {
        val playerState = player.state.value
        val isOn = playerState is PlayerState.Buffering || playerState is PlayerState.Playing
        val isThisStation = playerState.station?.id == station.id
        if (isOn && isThisStation) player.stop() else player.play(station)
    }

    override fun onCleared() {
        player.release()
    }

    private fun load() {
        viewModelScope.launch {
            content.value = when (val result = repository.stations()) {
                is DataResult.Failure -> StationsContent.Error(result.error)

                is DataResult.Success -> if (result.value.isEmpty()) {
                    StationsContent.Empty
                } else {
                    StationsContent.Loaded(result.value)
                }
            }
        }
    }
}
