package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.data.local.entity.TeamEntity
import cz.vanama.courtflow.domain.model.Team

/** Maps the domain [Team] to its Room cache row. */
fun Team.toEntity(): TeamEntity =
    TeamEntity(
        id = id,
        abbreviation = abbreviation,
        city = city,
        conference = conference,
        division = division,
        fullName = fullName,
        name = name,
    )

/** Maps the Room cache row back to the domain [Team]; the artwork URL is re-derived from the id. */
fun TeamEntity.toDomain(): Team =
    Team(
        id = id,
        abbreviation = abbreviation,
        city = city,
        conference = conference,
        division = division,
        fullName = fullName,
        name = name,
        imageUrl = PlaceholderImages.teamEmblem(id),
    )
