package cz.vanama.courtflow.domain.model

/**
 * NBA team as used by the presentation layer.
 */
data class Team(
    val id: Int,
    val abbreviation: String,
    val city: String,
    val conference: String,
    val division: String,
    val fullName: String,
    val name: String,
    /** Ready-to-load artwork URL; resolved by the data layer. */
    val imageUrl: String = "",
)
