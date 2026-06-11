package cz.vanama.courtflow.domain.model

/**
 * NBA player as used by the presentation layer.
 *
 * Optional attributes ([height], [weight], [jerseyNumber], [college], [country]
 * and the draft fields) are `null` when the API does not know them.
 */
data class Player(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val position: String,
    val height: String? = null,
    val weight: String? = null,
    val jerseyNumber: String? = null,
    val college: String? = null,
    val country: String? = null,
    val draftYear: Int? = null,
    val draftRound: Int? = null,
    val draftNumber: Int? = null,
    val team: Team,
)
