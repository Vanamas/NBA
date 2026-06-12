package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.repository.GameRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * [GameRepository] backed by the balldontlie REST API.
 *
 * The `/games` endpoint has no sort parameter and its cursor walks games in
 * ascending order from the first NBA season, so "recent" is implemented as a
 * single request limited to a [WINDOW_DAYS]-day `start_date`/`end_date`
 * window (one page of [PAGE_SIZE] always covers a team's games in that
 * span), filtered to completed games and sorted by date client-side. During
 * the off-season the window may be empty — the UI hides the section.
 * [nowMillis] is injectable for deterministic tests.
 */
class GameRepositoryImpl(
    private val api: NBAApi,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : GameRepository {
    override suspend fun getRecentGames(teamId: Int): List<Game> =
        safeApiCall {
            api
                .nbaV1GamesGet(
                    perPage = PAGE_SIZE,
                    teamIds = listOf(teamId),
                    startDate = isoDate(nowMillis() - TimeUnit.DAYS.toMillis(WINDOW_DAYS)),
                    endDate = isoDate(nowMillis()),
                ).data
                .orEmpty()
                .map { it.toDomain() }
                .filter { it.status == STATUS_FINAL }
                .sortedByDescending { it.date }
                .take(RECENT_GAMES_LIMIT)
        }

    /** Formats [epochMillis] as the API's `yyyy-MM-dd`, pinned to UTC for determinism. */
    private fun isoDate(epochMillis: Long): String =
        SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(epochMillis))

    private companion object {
        /** One page comfortably covers a team's games within [WINDOW_DAYS] (≤ ~25). */
        const val PAGE_SIZE = 50
        const val RECENT_GAMES_LIMIT = 5
        const val WINDOW_DAYS = 45L
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"

        /** The API's status for completed games; otherwise it carries tip-off time or quarter info. */
        const val STATUS_FINAL = "Final"
    }
}
