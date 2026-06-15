package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAStandings
import cz.vanama.courtflow.domain.model.Standing

/**
 * Maps the network [NBAStandings] to the domain [Standing] model.
 *
 * The official OpenAPI definition marks every attribute as optional, so the
 * mapper enforces the [NBAStandings.team] invariant (the team the standing
 * belongs to must be present) and falls back to `0` for missing numbers and
 * an empty string for the conference name.
 */
fun NBAStandings.toDomain(): Standing {
    val team = requireNotNull(team) { "Standing is missing a team" }
    return Standing(
        teamId = requireNotNull(team.id) { "Standing team is missing an id" },
        wins = wins ?: 0,
        losses = losses ?: 0,
        conferenceRank = conferenceRank ?: 0,
        conference = team.conference?.value.orEmpty(),
    )
}
