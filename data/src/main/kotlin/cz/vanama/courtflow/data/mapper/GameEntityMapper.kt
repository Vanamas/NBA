package cz.vanama.courtflow.data.mapper

import cz.vanama.courtflow.data.local.entity.GameEntity
import cz.vanama.courtflow.domain.model.Game

/** Maps a domain [Game] to its Room cache row for [teamId]'s recent-games list. */
fun Game.toEntity(teamId: Int): GameEntity =
    GameEntity(
        teamId = teamId,
        id = id,
        date = date,
        homeTeam = homeTeam.toEntity(),
        homeTeamScore = homeTeamScore,
        visitorTeam = visitorTeam.toEntity(),
        visitorTeamScore = visitorTeamScore,
    )

/** Maps the Room cache row back to the domain [Game]; team artwork is re-derived in [TeamEntity.toDomain]. */
fun GameEntity.toDomain(): Game =
    Game(
        id = id,
        date = date,
        homeTeam = homeTeam.toDomain(),
        homeTeamScore = homeTeamScore,
        visitorTeam = visitorTeam.toDomain(),
        visitorTeamScore = visitorTeamScore,
    )
