package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.domain.model.Player

/**
 * Maps the network [NBAPlayer] to the domain [Player] model.
 *
 * The official OpenAPI definition marks every attribute as optional, so the
 * mapper enforces the invariants the app relies on: [NBAPlayer.id] and
 * [NBAPlayer.team] must be present (the API always sends them), text
 * attributes fall back to empty strings.
 */
fun NBAPlayer.toDomain(): Player =
    Player(
        id = requireNotNull(id) { "Player is missing an id" },
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        position = position.orEmpty(),
        height = height,
        weight = weight,
        jerseyNumber = jerseyNumber,
        college = college,
        country = country,
        draftYear = draftYear,
        draftRound = draftRound,
        draftNumber = draftNumber,
        team = requireNotNull(team) { "Player $id is missing a team" }.toDomain(),
    )
