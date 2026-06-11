package cz.vanama.courtflow.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Envelope of single-resource balldontlie responses: `{"data": {...}}`.
 */
@JsonClass(generateAdapter = true)
data class SingleResponse<T>(
    @Json(name = "data") val data: T
)

/**
 * Envelope of list balldontlie responses: `{"data": [...], "meta": {...}}`.
 */
@JsonClass(generateAdapter = true)
data class CommonResponse<T>(
    @Json(name = "data") val data: List<T>,
    @Json(name = "meta") val meta: MetaDto? = null
)

/**
 * Paging metadata of list responses; [nextCursor] is `null` on the last page.
 */
@JsonClass(generateAdapter = true)
data class MetaDto(
    @Json(name = "next_cursor") val nextCursor: Int?,
    @Json(name = "per_page") val perPage: Int?
)
