package cz.vanama.courtflow.widget

import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase

/**
 * Pure, unit-testable widget data layer: resolves the favorite team via
 * [FavoriteTeamProvider], asks [GetTeamGamesUseCase] for its recent games
 * (newest first), and maps the latest one to a [WidgetUiModel]. Any failure —
 * including no configured favorite — collapses to [WidgetUiModel.Error] so the
 * Glance composable never has to branch on exceptions.
 */
class WidgetDataLoader(
    private val favoriteTeamProvider: FavoriteTeamProvider,
    private val getTeamGames: GetTeamGamesUseCase,
) {
    /** Resolves the model the widget should render right now. Never throws. */
    suspend fun load(): WidgetUiModel {
        val teamId = favoriteTeamProvider.favoriteTeamId() ?: return WidgetUiModel.Error
        return runCatching { getTeamGames(teamId) }
            .map { games -> games.firstOrNull().toModel(teamId) }
            .getOrElse { WidgetUiModel.Error }
    }

    private fun Game?.toModel(teamId: Int): WidgetUiModel {
        if (this == null) return WidgetUiModel.NoRecentGame(teamId = teamId, teamName = "")
        return WidgetUiModel.Score(
            teamId = teamId,
            teamName = homeTeam.fullName,
            scoreLine = formatScoreLine(),
        )
    }

    /** Renders a game as "HOME homeScore - visitorScore VISITOR" using abbreviations. */
    private fun Game.formatScoreLine(): String =
        "${homeTeam.abbreviation} $homeTeamScore - $visitorTeamScore ${visitorTeam.abbreviation}"
}
