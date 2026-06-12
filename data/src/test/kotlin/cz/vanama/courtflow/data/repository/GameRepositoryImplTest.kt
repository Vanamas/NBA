package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAGame
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1GamesGet200Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GameRepositoryImplTest {
    private val api: NBAApi = mockk()
    private val repository = GameRepositoryImpl(api) { FIXED_NOW_MILLIS }

    @Test
    fun `requests one page of the team's games within the recent window`() =
        runTest {
            coEvery {
                api.nbaV1GamesGet(
                    perPage = 50,
                    teamIds = listOf(10),
                    startDate = "2026-04-28",
                    endDate = "2026-06-12",
                )
            } returns NbaV1GamesGet200Response(data = emptyList())

            val result = repository.getRecentGames(10)

            assertEquals(emptyList<Any>(), result)
            coVerify(exactly = 1) {
                api.nbaV1GamesGet(
                    perPage = 50,
                    teamIds = listOf(10),
                    startDate = "2026-04-28",
                    endDate = "2026-06-12",
                )
            }
        }

    @Test
    fun `returns only completed games, newest first, capped at five`() =
        runTest {
            val dtos =
                listOf(
                    finalGame(id = 1, date = "2026-06-01"),
                    finalGame(id = 2, date = "2026-06-09"),
                    // Scheduled game: the API carries the tip-off time in `status`.
                    finalGame(id = 3, date = "2026-06-12").copy(status = "2026-06-12T19:00:00Z"),
                    finalGame(id = 4, date = "2026-06-05"),
                    finalGame(id = 5, date = "2026-06-11"),
                    finalGame(id = 6, date = "2026-06-03"),
                    finalGame(id = 7, date = "2026-06-07"),
                )
            coEvery {
                api.nbaV1GamesGet(
                    perPage = 50,
                    teamIds = listOf(10),
                    startDate = "2026-04-28",
                    endDate = "2026-06-12",
                )
            } returns NbaV1GamesGet200Response(data = dtos)

            val result = repository.getRecentGames(10)

            assertEquals(listOf(5, 2, 7, 4, 6), result.map { it.id })
            assertEquals(
                listOf("2026-06-11", "2026-06-09", "2026-06-07", "2026-06-05", "2026-06-03"),
                result.map { it.date },
            )
        }

    private fun finalGame(
        id: Int,
        date: String,
    ): NBAGame =
        NBAGame(
            id = id,
            date = date,
            status = "Final",
            homeTeamScore = 112,
            visitorTeamScore = 99,
            homeTeam = warriorsDto,
            visitorTeam = lakersDto,
        )

    private val warriorsDto =
        NBATeam(
            id = 10,
            abbreviation = "GSW",
            city = "Golden State",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Golden State Warriors",
            name = "Warriors",
        )

    private val lakersDto =
        NBATeam(
            id = 14,
            abbreviation = "LAL",
            city = "Los Angeles",
            conference = NBATeam.Conference.West,
            division = NBATeam.Division.Pacific,
            fullName = "Los Angeles Lakers",
            name = "Lakers",
        )

    private companion object {
        /** 2026-06-12T12:00:00Z — keeps the formatted UTC window deterministic. */
        const val FIXED_NOW_MILLIS = 1_781_265_600_000L
    }
}
