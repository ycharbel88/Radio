package com.chy.radio.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chy.radio.domain.AudioPlayer
import com.chy.radio.domain.DataError
import com.chy.radio.domain.DataResult
import com.chy.radio.domain.PlayerState
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationKind
import com.chy.radio.domain.StationRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// viewModelScope runs on Dispatchers.Main, which tests replace. runTest shares its
// scheduler, so advanceUntilIdle() also runs the ViewModel's coroutines.
@OptIn(ExperimentalCoroutinesApi::class)
class StationsViewModelTest {
    private val fip = station("FIP", brandName = "FIP")
    private val fipRock = station("FIP_ROCK", brandName = "FIP")
    private val inter = station("FRANCEINTER", brandName = "France Inter")

    private val repository = TestRepository(DataResult.Success(listOf(inter, fip, fipRock)))
    private val player = TestPlayer()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts in Loading before the repository answers`() = runTest {
        val viewModel = StationsViewModel(repository, player)

        assertEquals(StationsContent.Loading, viewModel.uiState.value.content)
    }

    @Test
    fun `shows the stations in API order when the repository succeeds`() = runTest {
        val viewModel = StationsViewModel(repository, player)

        advanceUntilIdle()

        val expected = StationsContent.Loaded(listOf(inter, fip, fipRock))
        assertEquals(expected, viewModel.uiState.value.content)
    }

    @Test
    fun `shows Empty when the repository returns no station`() = runTest {
        repository.result = DataResult.Success(emptyList())
        val viewModel = StationsViewModel(repository, player)

        advanceUntilIdle()

        assertEquals(StationsContent.Empty, viewModel.uiState.value.content)
    }

    @Test
    fun `shows the error when the repository fails`() = runTest {
        repository.result = DataResult.Failure(DataError.Network)
        val viewModel = StationsViewModel(repository, player)

        advanceUntilIdle()

        assertEquals(StationsContent.Error(DataError.Network), viewModel.uiState.value.content)
    }

    @Test
    fun `reloads on retry after an error`() = runTest {
        repository.result = DataResult.Failure(DataError.Network)
        val viewModel = StationsViewModel(repository, player)
        advanceUntilIdle()
        repository.result = DataResult.Success(listOf(fip))

        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(StationsContent.Loaded(listOf(fip)), viewModel.uiState.value.content)
    }

    @Test
    fun `mirrors the player state in the ui state`() = runTest {
        val viewModel = StationsViewModel(repository, player)

        player.state.value = PlayerState.Buffering(fip)
        advanceUntilIdle()

        assertEquals(PlayerState.Buffering(fip), viewModel.uiState.value.playerState)
    }

    @Test
    fun `plays a station when nothing is playing`() = runTest {
        val viewModel = StationsViewModel(repository, player)

        viewModel.onStationClick(fip)

        assertEquals(PlayerState.Playing(fip), player.state.value)
    }

    @Test
    fun `stops when the playing station is tapped again`() = runTest {
        player.state.value = PlayerState.Playing(fip)
        val viewModel = StationsViewModel(repository, player)

        viewModel.onStationClick(fip)

        assertEquals(PlayerState.Idle, player.state.value)
    }

    @Test
    fun `switches to another station when a different one is tapped`() = runTest {
        player.state.value = PlayerState.Playing(fip)
        val viewModel = StationsViewModel(repository, player)

        viewModel.onStationClick(fipRock)

        assertEquals(PlayerState.Playing(fipRock), player.state.value)
    }

    @Test
    fun `releases the player when the ViewModel is cleared`() = runTest {
        // Clearing the store is what Android does when the screen goes away for good.
        val store = ViewModelStore()
        val factory = viewModelFactory { initializer { StationsViewModel(repository, player) } }
        ViewModelProvider.create(store, factory).get(StationsViewModel::class)

        store.clear()

        assertTrue(player.isReleased)
    }
}

private class TestRepository(var result: DataResult<List<Station>>) : StationRepository {
    override suspend fun stations(): DataResult<List<Station>> = result
}

// An ideal player: play and stop take effect at once. Tests set `state` directly for
// what a real player reports on its own (buffering, errors).
private class TestPlayer : AudioPlayer {
    override val state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    var isReleased = false

    override fun play(station: Station) {
        state.value = PlayerState.Playing(station)
    }

    override fun stop() {
        state.value = PlayerState.Idle
    }

    override fun release() {
        isReleased = true
    }
}

private fun station(id: String, brandName: String) = Station(
    id = id,
    name = id,
    description = null,
    streamUrl = "https://icecast.radiofrance.fr/$id.mp3",
    kind = StationKind.WEBRADIO,
    brandId = brandName,
    brandName = brandName
)
