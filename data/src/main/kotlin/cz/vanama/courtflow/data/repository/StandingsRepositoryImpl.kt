package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.data.mapper.toDomain
import cz.vanama.courtflow.domain.model.Standing
import cz.vanama.courtflow.domain.repository.StandingsRepository
import java.util.Calendar
import java.util.TimeZone

/**
 * [StandingsRepository] backed by the balldontlie REST API.
 *
 * The `/standings` endpoint is league-wide and takes a required `season`
 * (the year the season *starts*). The repository derives the current season
 * from [nowMillis] — the regular season spans roughly October→June, so the
 * season year is `year` from October and `year - 1` for January→September —
 * fetches the league standings, and filters to the requested team, returning
 * `null` when the team has no published standing yet. [nowMillis] is
 * injectable for deterministic tests.
 */
class StandingsRepositoryImpl(
    private val api: NBAApi,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : StandingsRepository {
    override suspend fun getTeamStanding(teamId: Int): Standing? =
        safeApiCall {
            // Map every row first so a malformed standing surfaces as a
            // DataException (via safeApiCall) rather than being silently
            // dropped, then pick the requested team — `null` when absent.
            api
                .nbaV1StandingsGet(season = currentSeason())
                .data
                .orEmpty()
                .map { it.toDomain() }
                .firstOrNull { it.teamId == teamId }
        }

    /** The starting year of the current NBA season for [nowMillis]. */
    private fun currentSeason(): Int {
        val calendar =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = nowMillis() }
        val year = calendar.get(Calendar.YEAR)
        // Calendar months are 0-based; October == 9. From October the new
        // season has started; before then we are still in the prior season.
        return if (calendar.get(Calendar.MONTH) >= SEASON_START_MONTH) year else year - 1
    }

    private companion object {
        /** October (0-based) — the month the NBA regular season tips off. */
        const val SEASON_START_MONTH = 9
    }
}
