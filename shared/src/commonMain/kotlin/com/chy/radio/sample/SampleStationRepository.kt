package com.chy.radio.sample

import com.chy.radio.domain.DataResult
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationKind
import com.chy.radio.domain.StationRepository
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/** Long enough to see the loading state on screen, short enough not to annoy. */
private val SAMPLE_LOADING_DELAY = 1.seconds

/**
 * TEMPORARY, deleted in step 2 (replaced by the Apollo repository).
 *
 * Returns a few real stations so the screen can be built and tried without the network.
 * Ids, names, descriptions and streams come from the API response recorded on 2026-11-10.
 */
class SampleStationRepository : StationRepository {
    override suspend fun stations(): DataResult<List<Station>> {
        delay(SAMPLE_LOADING_DELAY)
        return DataResult.Success(sampleStations)
    }
}

private val sampleStations = listOf(
    Station(
        id = "FRANCEINTER",
        name = "France Inter",
        description = "Joyeuse, savante et populaire, " +
            "France Inter est la radio généraliste de service public",
        streamUrl = "https://icecast.radiofrance.fr/franceinter-midfi.mp3?id=openapi",
        kind = StationKind.NATIONAL,
        brandId = "FRANCEINTER",
        brandName = "France Inter"
    ),
    Station(
        id = "FRANCECULTURE",
        name = "France Culture",
        // The API sends "" for this brand; the mapping turns blank into null.
        description = null,
        streamUrl = "https://icecast.radiofrance.fr/franceculture-lofi.mp3?id=openapi",
        kind = StationKind.NATIONAL,
        brandId = "FRANCECULTURE",
        brandName = "France Culture"
    ),
    Station(
        id = "FIP",
        name = "FIP",
        description = "La radio musicale la plus éclectique.",
        streamUrl = "https://icecast.radiofrance.fr/fip-midfi.mp3?id=openapi",
        kind = StationKind.NATIONAL,
        brandId = "FIP",
        brandName = "FIP"
    ),
    Station(
        id = "FIP_ROCK",
        name = "FIP Rock",
        description = "De Bowie à Radiohead, de Lou Reed à Miossec, FIP conjugue tous les rocks " +
            "dans une sélection musicale qui traverse les genres et les époques.",
        streamUrl = "https://icecast.radiofrance.fr/fiprock-midfi.mp3?id=openapi",
        kind = StationKind.WEBRADIO,
        brandId = "FIP",
        brandName = "FIP"
    ),
    Station(
        id = "FIP_JAZZ",
        name = "FIP Jazz",
        description = "D’Avishai Cohen à Herbie Hancock, de Nina Simone à Christian Scott, " +
            "retrouvez le swing de FIP dans une sélection musicale qui traverse les genres " +
            "et les époques.",
        streamUrl = "https://icecast.radiofrance.fr/fipjazz-midfi.mp3?id=openapi",
        kind = StationKind.WEBRADIO,
        brandId = "FIP",
        brandName = "FIP"
    )
)
