package cz.vanama.courtflow.widget

import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetTeamGamesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetDataLoaderTest {
    private val getTeamGames = mockk<GetTeamGamesUseCase>()

    private fun loader(favoriteTeamId: Int?): WidgetDataLoader =
        WidgetDataLoader(
            favoriteTeamProvider = { favoriteTeamId },
            getTeamGames = getTeamGames,
        )

    private fun team(id: Int, abbr: String) =
        Team(
            id = id,
            abbreviation = abbr,
            city = "",
            conference = "",
            division = "",
            fullName = "$abbr Team",
            name = abbr,
        )

    private fun game(
        id: Int,
        date: String,
        home: Team,
        homeScore: Int,
        visitor: Team,
        visitorScore: Int,
    ) = Game(
        id = id,
        date = date,
        homeTeam = home,
        homeTeamScore = homeScore,
        visitorTeam = visitor,
        visitorTeamScore = visitorScore,
    )

    @Test
    fun `maps newest game to a score line keyed by the favorite team id`() = runTest {
        val hawks = team(1, "ATL")
        val lakers = team(2, "LAL")
        // Use case already returns newest-first; the loader takes first().
        coEvery { getTeamGames(1) } returns
            listOf(
                game(99, "2026-06-14", home = hawks, homeScore = 110, visitor = lakers, visitorScore = 104),
                game(98, "2026-06-10", home = lakers, homeScore = 90, visitor = hawks, visitorScore = 88),
            )

        val model = loader(favoriteTeamId = 1).load()

        assertEquals(
            WidgetUiModel.Score(
                teamId = 1,
                teamName = "ATL Team",
                scoreLine = "ATL 110 - 104 LAL",
            ),
            model,
        )
    }

    @Test
    fun `score line is oriented home-abbr home-score - visitor-score visitor-abbr`() = runTest {
        val hawks = team(1, "ATL")
        val celtics = team(3, "BOS")
        // Favorite team is the visitor here; line still reads home -> visitor.
        coEvery { getTeamGames(1) } returns
            listOf(game(50, "2026-06-14", home = celtics, homeScore = 99, visitor = hawks, visitorScore = 101))

        val model = loader(favoriteTeamId = 1).load()

        assertEquals(
            WidgetUiModel.Score(
                teamId = 1,
                teamName = "BOS Team",
                scoreLine = "BOS 99 - 101 ATL",
            ),
            model,
        )
    }

    @Test
    fun `no recent game yields NoRecentGame carrying the team id from the first game-less fetch`() = runTest {
        coEvery { getTeamGames(1) } returns emptyList()

        val model = loader(favoriteTeamId = 1).load()

        // With no games we cannot derive the team name from a Game, so the
        // loader reports NoRecentGame with the id only and an empty name.
        assertEquals(WidgetUiModel.NoRecentGame(teamId = 1, teamName = ""), model)
    }

    @Test
    fun `null favorite team id yields Error without calling the use case`() = runTest {
        val model = loader(favoriteTeamId = null).load()

        assertEquals(WidgetUiModel.Error, model)
    }

    @Test
    fun `use case failure yields Error`() = runTest {
        coEvery { getTeamGames(1) } throws RuntimeException("network down")

        val model = loader(favoriteTeamId = 1).load()

        assertEquals(WidgetUiModel.Error, model)
    }
}
