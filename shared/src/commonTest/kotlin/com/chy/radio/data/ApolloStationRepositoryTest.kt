package com.chy.radio.data

import com.apollographql.apollo.exception.ApolloNetworkException
import com.chy.radio.domain.DataError
import com.chy.radio.domain.DataResult
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ApolloStationRepositoryTest {
    private val apiKey = "00000000-0000-0000-0000-000000000000"

    private fun repository(engine: FakeHttpEngine, key: String = apiKey) =
        ApolloStationRepository(key, engine)

    private fun DataResult<List<Station>>.stations(): List<Station> =
        assertIs<DataResult.Success<List<Station>>>(this).value

    private fun DataResult<List<Station>>.error(): DataError =
        assertIs<DataResult.Failure>(this).error

    // --- Mapping (recorded response) ---

    @Test
    fun `maps the recorded response to 77 stations`() = runTest {
        val stations = repository(FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) })
            .stations()
            .stations()

        assertEquals(77, stations.size)
        assertEquals(6, stations.count { it.kind == StationKind.NATIONAL })
        assertEquals(26, stations.count { it.kind == StationKind.WEBRADIO })
        assertEquals(45, stations.count { it.kind == StationKind.LOCAL })
    }

    @Test
    fun `a brand without a stream is not a station but its radios are`() = runTest {
        val stations = repository(FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) })
            .stations()
            .stations()

        assertTrue(stations.none { it.id == "FRANCEBLEU" })
        val alsace = stations.single { it.id == "FRANCEBLEU_ALSACE" }
        assertEquals(StationKind.LOCAL, alsace.kind)
        assertEquals("FRANCEBLEU", alsace.brandId)
        assertEquals("ICI", alsace.brandName)
    }

    @Test
    fun `keeps API order with each brand before its webradios`() = runTest {
        val stations = repository(FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) })
            .stations()
            .stations()

        assertEquals("FRANCEINTER", stations[0].id)
        assertEquals("FRANCEINTER_LA_MUSIQUE_INTER", stations[1].id)
        assertEquals(StationKind.WEBRADIO, stations[1].kind)
        assertEquals("France Inter", stations[1].brandName)
    }

    @Test
    fun `stream URLs are kept as given including the France Culture lofi one`() = runTest {
        val stations = repository(FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) })
            .stations()
            .stations()

        assertEquals(
            "https://icecast.radiofrance.fr/franceculture-lofi.mp3?id=openapi",
            stations.single { it.id == "FRANCECULTURE" }.streamUrl
        )
    }

    @Test
    fun `descriptions are trimmed and blank ones become null`() = runTest {
        val stations = repository(FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) })
            .stations()
            .stations()

        // The API sends a trailing space here.
        val inter = stations.single { it.id == "FRANCEINTER" }.description
        assertEquals(inter?.trim(), inter)
        assertTrue(inter.orEmpty().startsWith("Joyeuse, savante et populaire"))
        // The API sends "" for most local radios.
        assertNull(stations.single { it.id == "FRANCEBLEU_ALSACE" }.description)
    }

    // --- Auth ---

    @Test
    fun `sends the key in the X-Token header and not in the URL`() = runTest {
        val engine = FakeHttpEngine { jsonResponse(200, STATIONS_RESPONSE_JSON) }

        repository(engine).stations()

        val request = engine.requests.single()
        assertEquals(apiKey, request.headers.single { it.name == "X-Token" }.value)
        assertFalse(request.url.contains(apiKey))
    }

    @Test
    fun `blank key gives MissingApiKey without any request`() = runTest {
        val engine = FakeHttpEngine { error("must not be called") }

        val result = repository(engine, key = " ").stations()

        assertEquals(DataError.MissingApiKey, result.error())
        assertTrue(engine.requests.isEmpty())
    }

    // --- Errors ---

    @Test
    fun `403 with the recorded body gives InvalidApiKey`() = runTest {
        val result = repository(FakeHttpEngine { jsonResponse(403, INVALID_KEY_RESPONSE_JSON) })
            .stations()

        assertEquals(DataError.InvalidApiKey, result.error())
    }

    @Test
    fun `429 gives QuotaExceeded`() = runTest {
        val result = repository(
            FakeHttpEngine {
                jsonResponse(429, """{"message":"Too Many Requests"}""")
            }
        ).stations()

        assertEquals(DataError.QuotaExceeded, result.error())
    }

    @Test
    fun `other HTTP statuses give Server with the status`() = runTest {
        val result = repository(FakeHttpEngine { jsonResponse(500, """{"message":"boom"}""") })
            .stations()

        assertEquals(DataError.Server(httpStatus = 500), result.error())
    }

    @Test
    fun `GraphQL errors without data give Server without status`() = runTest {
        // Shape from the GraphQL spec; not observed on this API.
        val result = repository(
            FakeHttpEngine {
                jsonResponse(200, """{"data":null,"errors":[{"message":"internal"}]}""")
            }
        ).stations()

        assertEquals(DataError.Server(httpStatus = null), result.error())
    }

    @Test
    fun `connection failure gives Network`() = runTest {
        val result = repository(
            FakeHttpEngine {
                throw ApolloNetworkException("Failed to connect")
            }
        ).stations()

        assertEquals(DataError.Network, result.error())
    }
}
