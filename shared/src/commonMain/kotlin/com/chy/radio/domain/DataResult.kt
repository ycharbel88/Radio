package com.chy.radio.domain

/**
 * The outcome of a data call. Like `kotlin.Result`, but the failure is a typed
 * [DataError] instead of a Throwable, so `when` is exhaustive with no cast.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val value: T) : DataResult<T>

    data class Failure(val error: DataError) : DataResult<Nothing>
}

/**
 * Why data could not be loaded. The UI maps each case to a message; it never
 * sees an HTTP or GraphQL type.
 */
sealed interface DataError {
    /** No key in local.properties. Detected before any network call. */
    data object MissingApiKey : DataError

    /** The API refused the key (observed: HTTP 403, `{"message":"Unauthorized: Invalid x-token"}`). */
    data object InvalidApiKey : DataError

    /** Daily quota (1000 requests) used up. */
    data object QuotaExceeded : DataError

    /** No connection, timeout, DNS failure... Worth a retry. */
    data object Network : DataError

    /** Any other server-side failure. [httpStatus] is null for a GraphQL error on a 200. */
    data class Server(val httpStatus: Int?) : DataError
}
