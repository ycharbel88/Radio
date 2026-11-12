package com.chy.radio.domain

data class Station(
    val id: String,
    val name: String,
    val description: String?,
    val streamUrl: String,
    val kind: StationKind,
    val brandId: String,
    val brandName: String
)

/** Where a station comes from in the API. Drives the row subtitle. */
enum class StationKind {
    NATIONAL,
    WEBRADIO,
    LOCAL
}
