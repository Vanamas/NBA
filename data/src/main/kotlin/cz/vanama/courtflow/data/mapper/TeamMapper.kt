package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.domain.model.Team

/**
 * Maps the network [NBATeam] to the domain [Team] model.
 *
 * The official OpenAPI definition marks every attribute as optional, so the
 * mapper enforces the [NBATeam.id] invariant and falls back to empty strings
 * for missing text attributes; conference/division enums map to their wire
 * values.
 */
fun NBATeam.toDomain(): Team =
    Team(
        id = requireNotNull(id) { "Team is missing an id" },
        abbreviation = abbreviation.orEmpty(),
        city = city.orEmpty(),
        conference = conference?.value.orEmpty(),
        division = division?.value.orEmpty(),
        fullName = fullName.orEmpty(),
        name = name.orEmpty(),
    )
