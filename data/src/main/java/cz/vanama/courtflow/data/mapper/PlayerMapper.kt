package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.model.PlayerDto
import cz.vanama.courtflow.domain.model.Player

/** Maps the network [PlayerDto] to the domain [Player] model. */
fun PlayerDto.toDomain(): Player {
    return Player(
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
        team = team.toDomain()
    )
}
