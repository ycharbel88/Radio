package com.chy.radio

import com.chy.radio.domain.DataResult
import com.chy.radio.domain.Station
import com.chy.radio.domain.StationRepository

class FakeStationRepository(var result: DataResult<List<Station>>) : StationRepository {
    override suspend fun stations(): DataResult<List<Station>> = result
}
