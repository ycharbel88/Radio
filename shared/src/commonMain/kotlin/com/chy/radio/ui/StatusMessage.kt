package com.chy.radio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chy.radio.domain.DataError
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import radio.shared.generated.resources.Res
import radio.shared.generated.resources.action_retry
import radio.shared.generated.resources.error_invalid_api_key
import radio.shared.generated.resources.error_missing_api_key
import radio.shared.generated.resources.error_network
import radio.shared.generated.resources.error_quota_exceeded
import radio.shared.generated.resources.error_server

private val MESSAGE_TOP_PADDING = 48.dp
private val MESSAGE_SPACING = 16.dp

/** Shown in place of the list. "Réessayer" appears only when [onRetry] is given. */
@Composable
internal fun StatusMessage(
    message: String,
    isLoading: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SCREEN_HORIZONTAL_PADDING)
            .padding(top = MESSAGE_TOP_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MESSAGE_SPACING)
    ) {
        if (isLoading) CircularProgressIndicator()
        Text(message, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Button(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
        }
    }
}

internal data class ErrorUi(val message: StringResource, val canRetry: Boolean)

// A retry only helps when the problem is temporary: not for the key or the daily quota.
internal fun errorUi(error: DataError): ErrorUi = when (error) {
    DataError.MissingApiKey -> ErrorUi(Res.string.error_missing_api_key, canRetry = false)
    DataError.InvalidApiKey -> ErrorUi(Res.string.error_invalid_api_key, canRetry = false)
    DataError.QuotaExceeded -> ErrorUi(Res.string.error_quota_exceeded, canRetry = false)
    DataError.Network -> ErrorUi(Res.string.error_network, canRetry = true)
    is DataError.Server -> ErrorUi(Res.string.error_server, canRetry = true)
}
