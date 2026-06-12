package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.core.network.generated.model.NBAGame
import cz.vanama.courtflow.domain.model.Game

/**
 * Maps the network [NBAGame] to the domain [Game] model.
 *
 * The official OpenAPI definition marks every attribute as optional, so the
 * mapper enforces the invariants the app relies on: [NBAGame.id],
 * [NBAGame.homeTeam] and [NBAGame.visitorTeam] must be present (the API
 * always sends them); texts fall back to empty strings, scores to zero.
 */
fun NBAGame.toDomain(): Game =
    Game(
        id = requireNotNull(id) { "Game is missing an id" },
        date = date.orEmpty(),
        homeTeam = requireNotNull(homeTeam) { "Game $id is missing a home team" }.toDomain(),
        homeTeamScore = homeTeamScore ?: 0,
        visitorTeam = requireNotNull(visitorTeam) { "Game $id is missing a visitor team" }.toDomain(),
        visitorTeamScore = visitorTeamScore ?: 0,
    )
