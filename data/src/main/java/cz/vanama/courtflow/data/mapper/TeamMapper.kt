package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.model.TeamDto
import cz.vanama.courtflow.domain.model.Team

/** Maps the network [TeamDto] to the domain [Team] model. */
fun TeamDto.toDomain(): Team {
    return Team(
        id = id,
        abbreviation = abbreviation,
        city = city,
        conference = conference,
        division = division,
        fullName = fullName,
        name = name
    )
}
