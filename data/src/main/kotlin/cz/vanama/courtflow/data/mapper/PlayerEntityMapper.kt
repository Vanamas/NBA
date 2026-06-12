package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.data.local.entity.PlayerEntity
import cz.vanama.courtflow.domain.model.Player

/** Maps the domain [Player] to its Room cache row. */
fun Player.toEntity(): PlayerEntity =
    PlayerEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        position = position,
        height = height,
        weight = weight,
        jerseyNumber = jerseyNumber,
        college = college,
        country = country,
        draftYear = draftYear,
        draftRound = draftRound,
        draftNumber = draftNumber,
        team = team.toEntity(),
    )

/** Maps the Room cache row back to the domain [Player]. */
fun PlayerEntity.toDomain(): Player =
    Player(
        id = id,
        firstName = firstName,
        lastName = lastName,
        position = position,
        height = height,
        weight = weight,
        jerseyNumber = jerseyNumber,
        college = college,
        country = country,
        draftYear = draftYear,
        draftRound = draftRound,
        draftNumber = draftNumber,
        team = team.toDomain(),
    )
