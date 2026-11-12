package com.chy.radio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chy.radio.domain.Station
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import radio.shared.generated.resources.Res
import radio.shared.generated.resources.app_title
import radio.shared.generated.resources.empty_message
import radio.shared.generated.resources.loading
import radio.shared.generated.resources.stations_live

// Same as ListItem's side padding, so titles line up with the rows' text.
internal val SCREEN_HORIZONTAL_PADDING = 16.dp
private val TITLE_VERTICAL_PADDING = 8.dp

@Composable
fun StationsScreen(viewModel: StationsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StationsList(state, onStationClick = viewModel::onStationClick, onRetry = viewModel::onRetry)
}

// One scrolling list for every state: a long message at 200 % font scrolls, never cut off.
@Composable
internal fun StationsList(
    state: StationsUiState,
    onStationClick: (Station) -> Unit,
    onRetry: () -> Unit
) {
    val content = state.content

    LazyColumn(contentPadding = WindowInsets.safeDrawing.asPaddingValues()) {
        item(key = "title") { ScreenTitle(content) }
        when (content) {
            StationsContent.Loading -> item(key = "loading") {
                StatusMessage(stringResource(Res.string.loading), isLoading = true)
            }

            StationsContent.Empty -> item(key = "empty") {
                StatusMessage(stringResource(Res.string.empty_message), onRetry = onRetry)
            }

            is StationsContent.Error -> item(key = "error") {
                val errorUi = errorUi(content.error)
                val retry = if (errorUi.canRetry) onRetry else null
                StatusMessage(stringResource(errorUi.message), onRetry = retry)
            }

            is StationsContent.Loaded -> {
                // One header per brand: display only, not a business rule. groupBy keeps API order.
                val stationsByBrand = content.stations.groupBy { station -> station.brandName }
                stationsByBrand.forEach { (brandName, stations) ->
                    item(key = "brand-$brandName") { BrandHeader(brandName) }
                    items(stations, key = { station -> station.id }) { station ->
                        StationRow(station, state.playerState) { onStationClick(station) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenTitle(content: StationsContent) {
    Column(
        Modifier.padding(
            horizontal = SCREEN_HORIZONTAL_PADDING,
            vertical = TITLE_VERTICAL_PADDING
        )
    ) {
        Text(
            stringResource(Res.string.app_title),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium
        )
        if (content is StationsContent.Loaded) {
            val count = content.stations.size
            Text(pluralStringResource(Res.plurals.stations_live, count, count))
        }
    }
}

@Composable
private fun BrandHeader(brandName: String) {
    Text(
        brandName,
        Modifier
            .padding(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = TITLE_VERTICAL_PADDING)
            .semantics { heading() },
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium
    )
}
