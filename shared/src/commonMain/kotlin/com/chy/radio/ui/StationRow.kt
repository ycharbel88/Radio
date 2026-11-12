package com.chy.radio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.chy.radio.domain.PlaybackError
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationKind
import com.chy.radio.domain.station
import org.jetbrains.compose.resources.stringResource
import radio.shared.generated.resources.Res
import radio.shared.generated.resources.playback_error_network
import radio.shared.generated.resources.playback_error_stream_unavailable
import radio.shared.generated.resources.player_buffering
import radio.shared.generated.resources.player_error
import radio.shared.generated.resources.player_playing
import radio.shared.generated.resources.subtitle_local
import radio.shared.generated.resources.subtitle_webradio

// Fits the trailing slot without making the row taller.
private val BUFFERING_INDICATOR_SIZE = 24.dp

// The current row has a tinted background AND a text status: never colour alone.
@Composable
internal fun StationRow(station: Station, playerState: PlayerState, onClick: () -> Unit) {
    // The player state only matters on the row of the station it is about.
    val rowState = if (playerState.station?.id == station.id) playerState else PlayerState.Idle
    val stateText = stateText(rowState)
    val subtitle = subtitleText(station)
    val playbackError = playbackErrorText(rowState)
    val hasTrailingStatus = rowState is PlayerState.Buffering || rowState is PlayerState.Playing
    val containerColor = when (rowState) {
        PlayerState.Idle -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    ListItem(
        headlineContent = { Text(station.name) },
        modifier = Modifier
            // clickable merges the children into one accessibility node: "name, subtitle, state".
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { if (stateText != null) stateDescription = stateText },
        supportingContent = if (subtitle != null || playbackError != null) {
            { SupportingLines(subtitle, playbackError) }
        } else {
            null
        },
        trailingContent = if (hasTrailingStatus) {
            { TrailingStatus(rowState, stateText.orEmpty()) }
        } else {
            null
        },
        colors = ListItemDefaults.colors(containerColor = containerColor)
    )
}

@Composable
private fun stateText(rowState: PlayerState): String? = when (rowState) {
    PlayerState.Idle -> null
    is PlayerState.Buffering -> stringResource(Res.string.player_buffering)
    is PlayerState.Playing -> stringResource(Res.string.player_playing)
    is PlayerState.Error -> stringResource(Res.string.player_error)
}

// Null for a national station: its name already is the brand.
@Composable
private fun subtitleText(station: Station): String? = when (station.kind) {
    StationKind.NATIONAL -> null
    StationKind.WEBRADIO -> stringResource(Res.string.subtitle_webradio, station.brandName)
    StationKind.LOCAL -> stringResource(Res.string.subtitle_local, station.brandName)
}

@Composable
private fun playbackErrorText(rowState: PlayerState): String? {
    if (rowState !is PlayerState.Error) return null
    val resource = when (rowState.reason) {
        PlaybackError.Network -> Res.string.playback_error_network
        PlaybackError.StreamUnavailable -> Res.string.playback_error_stream_unavailable
    }
    return stringResource(resource)
}

@Composable
private fun SupportingLines(subtitle: String?, playbackError: String?) {
    Column {
        if (subtitle != null) Text(subtitle)
        if (playbackError != null) Text(playbackError, color = MaterialTheme.colorScheme.error)
    }
}

// Hidden from screen readers: the row's stateDescription already says it.
@Composable
private fun TrailingStatus(rowState: PlayerState, stateText: String) {
    val hidden = Modifier.clearAndSetSemantics {}
    when (rowState) {
        is PlayerState.Buffering -> CircularProgressIndicator(hidden.size(BUFFERING_INDICATOR_SIZE))
        else -> Text(stateText, hidden, style = MaterialTheme.typography.labelLarge)
    }
}
