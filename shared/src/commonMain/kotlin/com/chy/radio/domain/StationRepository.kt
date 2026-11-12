package com.chy.radio.domain

/**
 * Where the stations come from. An interface so the ViewModel can be tested with a
 * fake, and so no Apollo type reaches the UI.
 */
interface StationRepository {
    /** Every playable station, in API order. Never throws: failures are a [DataResult.Failure]. */
    suspend fun stations(): DataResult<List<Station>>
}
