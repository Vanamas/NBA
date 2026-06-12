package cz.vanama.courtflow.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import cz.vanama.courtflow.domain.model.Player
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.domain.usecase.GetPlayerDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetPlayersUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamDetailUseCase
import cz.vanama.courtflow.domain.usecase.GetTeamsUseCase
import cz.vanama.courtflow.feature.players.di.playersFeatureModule
import cz.vanama.courtflow.feature.teams.di.teamsFeatureModule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers the navigation flows of [CourtFlowNavGraph] end to end on the JVM:
 * which screen each [Destination] renders, how taps push new destinations,
 * and that system back pops the stack. Use cases are Koin-mocked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CourtFlowNavGraphTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val team = Team(10, "GSW", "Golden State", "West", "Pacific", "Golden State Warriors", "Warriors")
    private val player = Player(id = 19, firstName = "Stephen", lastName = "Curry", position = "G", team = team)

    @Before
    fun setUp() {
        // CourtFlowApplication (instantiated by Robolectric) already started Koin
        // with the real modules; replace the graph with mocked use cases.
        stopKoin()
        startKoin {
            modules(
                module {
                    single<GetPlayersUseCase> {
                        mockk {
                            every { this@mockk.invoke(any()) } returns flowOf(PagingData.from(listOf(player)))
                            every { this@mockk.invoke(null) } returns flowOf(PagingData.from(listOf(player)))
                        }
                    }
                    single<GetPlayerDetailUseCase> { mockk { coEvery { this@mockk.invoke(any()) } returns player } }
                    single<GetTeamsUseCase> { mockk { every { this@mockk.invoke() } returns flowOf(listOf(team)) } }
                    single<GetTeamDetailUseCase> { mockk { coEvery { this@mockk.invoke(any()) } returns team } }
                },
                playersFeatureModule,
                teamsFeatureModule,
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun awaitPlayerRow() {
        // The list stream is debounced in the ViewModel; wait until the first page lands.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodes(hasText("Stephen Curry"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun `start destination renders the player list`() {
        composeRule.setContent { CourtFlowNavGraph() }

        awaitPlayerRow()
        composeRule.onNodeWithText("NBA Players").assertExists()
    }

    @Test
    fun `tapping a player navigates to the player detail`() {
        composeRule.setContent { CourtFlowNavGraph() }
        awaitPlayerRow()

        composeRule.onNodeWithText("Stephen Curry").performClick()

        composeRule.onNodeWithText("Player Details").assertExists()
    }

    @Test
    fun `system back from detail returns to the list`() {
        composeRule.setContent { CourtFlowNavGraph() }
        awaitPlayerRow()
        composeRule.onNodeWithText("Stephen Curry").performClick()
        composeRule.onNodeWithText("Player Details").assertExists()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("NBA Players").assertExists()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w1024dp-h800dp")
    fun `wide window keeps the list visible next to the detail`() {
        composeRule.setContent { CourtFlowNavGraph() }
        awaitPlayerRow()

        composeRule.onNodeWithText("Stephen Curry").performClick()

        composeRule.onNodeWithText("Player Details").assertExists()
        // The list pane stays composed next to the detail pane.
        composeRule.onNodeWithText("NBA Players").assertExists()
    }

    @Test
    @Config(sdk = [35], qualifiers = "w1024dp-h800dp")
    fun `wide window shows a placeholder detail pane before selection`() {
        composeRule.setContent { CourtFlowNavGraph() }
        awaitPlayerRow()

        composeRule.onNodeWithText("Select a player to see the details").assertExists()
    }

    @Test
    fun `deep linked detail opens with the list beneath it`() {
        composeRule.setContent {
            CourtFlowNavGraph(initialBackStack = listOf(Destination.PlayerList, Destination.PlayerDetail(19)))
        }

        composeRule.onNodeWithText("Player Details").assertExists()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("NBA Players").assertExists()
    }

    @Test
    fun `teams action opens the team list and back returns`() {
        composeRule.setContent { CourtFlowNavGraph() }
        awaitPlayerRow()

        composeRule.onNodeWithText("Teams").performClick()
        composeRule.onNodeWithText("NBA Teams").assertExists()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("NBA Players").assertExists()
    }
}
