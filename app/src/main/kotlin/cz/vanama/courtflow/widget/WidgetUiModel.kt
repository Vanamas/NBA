package cz.vanama.courtflow.widget

/**
 * Immutable render contract for [FavoriteTeamWidget]. The Glance composable is
 * a pure function of this model; all branching/IO happens in [WidgetDataLoader].
 */
sealed interface WidgetUiModel {
    /**
     * A resolved favorite team with its latest final score. [teamId] backs the
     * click deep link (`courtflow://team/{teamId}`); [scoreLine] is a fully
     * formatted, localization-agnostic line like "GSW 110 - 104 LAL".
     */
    data class Score(
        val teamId: Int,
        val teamName: String,
        val scoreLine: String,
    ) : WidgetUiModel

    /**
     * The favorite team is known but has no recent final game (off-season or
     * fresh schedule). Tapping still opens the team detail via [teamId].
     */
    data class NoRecentGame(
        val teamId: Int,
        val teamName: String,
    ) : WidgetUiModel

    /** Loading failed (network/auth). The widget shows a retry-on-tap message. */
    data object Error : WidgetUiModel
}
