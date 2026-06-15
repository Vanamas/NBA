package cz.vanama.courtflow.domain.model

/**
 * A single team's standing in the current NBA season as used by the
 * presentation layer: the win–loss record and the rank within the team's
 * conference. [conference] is the conference name (e.g. `West`) shown next
 * to the rank.
 */
data class Standing(
    val teamId: Int,
    val wins: Int,
    val losses: Int,
    val conferenceRank: Int,
    val conference: String,
)
