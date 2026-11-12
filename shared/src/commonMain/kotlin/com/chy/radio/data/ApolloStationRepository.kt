package com.chy.radio.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.apollo.network.http.HttpEngine
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.chy.radio.data.graphql.StationsQuery
import com.chy.radio.domain.DataError
import com.chy.radio.domain.DataResult
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationRepository

private const val RADIO_FRANCE_URL = "https://openapi.radiofrance.fr/v1/graphql"

/**
 * The stations from the Radio France Open API, one request per call (no cache).
 *
 * @param httpEngine OkHttp on Android, NSURLSession on iOS. Tests pass a fake one.
 */
internal class ApolloStationRepository(
    private val apiKey: String,
    httpEngine: HttpEngine = DefaultHttpEngine()
) : StationRepository {
    private val apolloClient = ApolloClient
        .Builder()
        .serverUrl(RADIO_FRANCE_URL)
        .httpEngine(httpEngine)
        .addHttpInterceptor(ApiKeyInterceptor(apiKey))
        .build()

    override suspend fun stations(): DataResult<List<Station>> {
        // CI and fresh clones have no key: say so clearly, and spend no request.
        if (apiKey.isBlank()) return DataResult.Failure(DataError.MissingApiKey)

        val response = apolloClient.query(StationsQuery()).execute()
        val data = response.data
        val exception = response.exception
        return when {
            data != null -> DataResult.Success(data.toStations())

            exception != null -> DataResult.Failure(exception.toDataError())

            // HTTP 200 carrying GraphQL errors and no data.
            else -> DataResult.Failure(DataError.Server(httpStatus = null))
        }
    }
}

// A header rather than the `x-token` query parameter (both work, checked on 2026-11-10):
// URLs end up in logs and proxies.
private class ApiKeyInterceptor(private val apiKey: String) : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse = chain.proceed(request.newBuilder().addHeader("X-Token", apiKey).build())
}
