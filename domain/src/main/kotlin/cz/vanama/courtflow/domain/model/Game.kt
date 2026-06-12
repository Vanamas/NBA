package cz.vanama.courtflow.domain.model

/**
 * A single NBA game as used by the presentation layer; [date] is the
 * ISO `yyyy-MM-dd` game date as delivered by the API.
 */
data class Game(
    val id: Int,
    val date: String,
    val status: String,
    val homeTeam: Team,
    val homeTeamScore: Int,
    val visitorTeam: Team,
    val visitorTeamScore: Int,
)
