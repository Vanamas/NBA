package cz.vanama.courtflow.feature.teams.detail

import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamStandingUseCase

/**
 * The read use cases the team detail screen loads in the ViewModel's `init`:
 * the team itself plus its two bonus sections (recent games and standing).
 * Bundling them keeps [TeamDetailViewModel]'s constructor focused (the
 * favorite use cases stay separate as a distinct capability).
 */
data class TeamDetailUseCases(
    val getTeamDetail: GetTeamDetailUseCase,
    val getTeamGames: GetTeamGamesUseCase,
    val getTeamStanding: GetTeamStandingUseCase,
)
