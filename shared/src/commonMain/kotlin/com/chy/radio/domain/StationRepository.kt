package com.chy.radio.domain

interface StationRepository {
    /** Every playable station, in API order. Never throws: failures are a [DataResult.Failure]. */
    suspend fun stations(): DataResult<List<Station>>
}
