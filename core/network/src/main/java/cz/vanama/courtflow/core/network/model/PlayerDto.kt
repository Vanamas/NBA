package cz.vanama.courtflow.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Player as returned by the balldontlie API; optional attributes are `null`
 * when the API does not know them.
 */
@JsonClass(generateAdapter = true)
data class PlayerDto(
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "position") val position: String,
    @Json(name = "height") val height: String? = null,
    @Json(name = "weight") val weight: String? = null,
    @Json(name = "jersey_number") val jerseyNumber: String? = null,
    @Json(name = "college") val college: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "draft_year") val draftYear: Int? = null,
    @Json(name = "draft_round") val draftRound: Int? = null,
    @Json(name = "draft_number") val draftNumber: Int? = null,
    @Json(name = "team") val team: TeamDto
)
