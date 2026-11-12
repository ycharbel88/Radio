package com.chy.radio.ui

import com.chy.radio.domain.DataError
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station

/** The list comes from the ViewModel; [playerState] is read live from the player. */
data class StationsUiState(val content: StationsContent, val playerState: PlayerState)

sealed interface StationsContent {
    data object Loading : StationsContent

    data object Empty : StationsContent

    data class Error(val error: DataError) : StationsContent

    /** Never empty: that case is [Empty]. */
    data class Loaded(val stations: List<Station>) : StationsContent
}
