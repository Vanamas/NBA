package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.common.error.DataErrorKind
import cz.vanama.courtflow.core.common.error.DataException
import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAStandings
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1StandingsGet200Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StandingsRepositoryImplTest {
    private val api: NBAApi = mockk()
    private val repository = StandingsRepositoryImpl(api) { FIXED_NOW_MILLIS }

    @Test
    fun `requests the current season and returns the matching team's standing`() =
        runTest {
            coEvery { api.nbaV1StandingsGet(season = 2025) } returns
                NbaV1StandingsGet200Response(
                    data = listOf(standingDto(teamId = 14, wins = 30, losses = 10), warriorsStanding),
                )

            val result = repository.getTeamStanding(10)

            assertEquals(10, result?.teamId)
            assertEquals(12, result?.wins)
            assertEquals(4, result?.losses)
            assertEquals(3, result?.conferenceRank)
            assertEquals("West", result?.conference)
            coVerify(exactly = 1) { api.nbaV1StandingsGet(season = 2025) }
        }

    @Test
    fun `returns null when the team is absent from the standings`() =
        runTest {
            coEvery { api.nbaV1StandingsGet(season = 2025) } returns
                NbaV1StandingsGet200Response(data = listOf(standingDto(teamId = 14, wins = 30, losses = 10)))

            val result = repository.getTeamStanding(10)

            assertNull(result)
        }

    @Test
    fun `returns null when the standings payload is empty`() =
        runTest {
            coEvery { api.nbaV1StandingsGet(season = 2025) } returns
                NbaV1StandingsGet200Response(data = emptyList())

            assertNull(repository.getTeamStanding(10))
        }

    @Test
    fun `translates a malformed standing payload into a DataException`() =
        runTest {
            // Matching team present but missing its nested team id -> mapper requireNotNull fails.
            coEvery { api.nbaV1StandingsGet(season = 2025) } returns
                NbaV1StandingsGet200Response(data = listOf(warriorsStanding.copy(team = null)))

            val thrown = runCatching { repository.getTeamStanding(10) }.exceptionOrNull()

            assertTrue("expected DataException, was $thrown", thrown is DataException)
            assertEquals(DataErrorKind.UNKNOWN, (thrown as DataException).kind)
        }

    private fun standingDto(
        teamId: Int,
        wins: Int,
        losses: Int,
    ): NBAStandings =
        NBAStandings(
            team =
                NBATeam(
                    id = teamId,
                    abbreviation = "LAL",
                    city = "Los Angeles",
                    conference = NBATeam.Conference.West,
                    division = NBATeam.Division.Pacific,
                    fullName = "Los Angeles Lakers",
                    name = "Lakers",
                ),
            wins = wins,
            losses = losses,
            conferenceRank = 1,
        )

    private val warriorsStanding =
        NBAStandings(
            team =
                NBATeam(
                    id = 10,
                    abbreviation = "GSW",
                    city = "Golden State",
                    conference = NBATeam.Conference.West,
                    division = NBATeam.Division.Pacific,
                    fullName = "Golden State Warriors",
                    name = "Warriors",
                ),
            wins = 12,
            losses = 4,
            conferenceRank = 3,
        )

    private companion object {
        /** 2026-06-12T12:00:00Z — June is in the 2025 season (Oct 2025 → Jun 2026). */
        const val FIXED_NOW_MILLIS = 1_781_265_600_000L
    }
}
