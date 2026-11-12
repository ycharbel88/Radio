package com.chy.radio.data

import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.exception.ApolloOfflineException
import com.chy.radio.domain.DataError

/** Apollo failure → [DataError]. The UI never sees an Apollo or HTTP type. */
internal fun ApolloException.toDataError(): DataError = when (this) {
    is ApolloHttpException -> httpStatusToDataError(statusCode)
    is ApolloNetworkException, is ApolloOfflineException -> DataError.Network
    else -> DataError.Server(httpStatus = null)
}

internal fun httpStatusToDataError(status: Int): DataError = when (status) {
    // Observed: 403 + {"message":"Unauthorized: Invalid x-token"} (2026-11-10).
    403 -> DataError.InvalidApiKey

    // Assumed HTTP standard for rate limiting, not observed.
    429 -> DataError.QuotaExceeded

    else -> DataError.Server(httpStatus = status)
}
