package cz.vanama.courtflow.feature.players.list

import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import kotlinx.coroutines.flow.Flow

/** UI state of the player list screen; the paging stream starts in the ViewModel's `init`. */
data class PlayerListState(
    val players: Flow<PagingData<Player>>,
    val searchQuery: String = "",
    val isOffline: Boolean = false,
    val retryInSeconds: Int? = null,
    /** Teams offered in the team picker; empty until the list loads. */
    val teams: List<Team> = emptyList(),
    /** Currently selected team filter, `null` when no team is selected. */
    val selectedTeam: Team? = null,
    /** Currently selected position code (`G`, `F`, `C`), `null` for any position. */
    val selectedPosition: String? = null,
)

/** User actions of the player list screen. */
sealed class PlayerListIntent {
    data class OnSearchQueryChanged(
        val query: String,
    ) : PlayerListIntent()

    data class OnTeamSelected(
        val team: Team?,
    ) : PlayerListIntent()

    data class OnPositionSelected(
        val position: String?,
    ) : PlayerListIntent()

    data class OnPlayerClicked(
        val playerId: Int,
    ) : PlayerListIntent()

    /** A paged load (refresh or append) failed with HTTP 429; carries the reset epoch. */
    data class OnRateLimited(
        val resetEpochSeconds: Long?,
    ) : PlayerListIntent()

    /** The rate-limited load is no longer in an error state (retried, loading, or succeeded). */
    data object OnRateLimitResolved : PlayerListIntent()
}

/** One-shot events emitted by [PlayerListViewModel]. */
sealed class PlayerListEffect {
    data class NavigateToPlayerDetail(
        val playerId: Int,
    ) : PlayerListEffect()

    data object RetryPaging : PlayerListEffect()
}
