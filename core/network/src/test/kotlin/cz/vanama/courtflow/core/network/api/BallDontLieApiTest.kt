package cz.vanama.courtflow.core.network.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class BallDontLieApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: BallDontLieApi

    @Before
    fun setup() {
        server = MockWebServer()
        val moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(BallDontLieApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getPlayers returns success`() =
        runTest {
            val json =
                """
                {
                  "data": [
                    {
                      "id": 1,
                      "first_name": "LeBron",
                      "last_name": "James",
                      "position": "F",
                      "team": {
                        "id": 14,
                        "abbreviation": "LAL",
                        "city": "Los Angeles",
                        "conference": "West",
                        "division": "Pacific",
                        "full_name": "Los Angeles Lakers",
                        "name": "Lakers"
                      }
                    }
                  ],
                  "meta": {
                    "next_cursor": 2,
                    "per_page": 35
                  }
                }
                """.trimIndent()
            server.enqueue(MockResponse().setBody(json))

            val response = api.getPlayers()

            assertEquals(1, response.data.size)
            assertEquals("LeBron", response.data[0].firstName)
            assertEquals(2, response.meta?.nextCursor)
        }

    @Test
    fun `getPlayer parses single player wrapped in data envelope`() =
        runTest {
            // Real response shape per https://docs.balldontlie.io - single resources are wrapped in "data"
            val json =
                """
                {
                  "data": {
                    "id": 19,
                    "first_name": "Stephen",
                    "last_name": "Curry",
                    "position": "G",
                    "height": "6-2",
                    "weight": "185",
                    "jersey_number": "30",
                    "college": "Davidson",
                    "country": "USA",
                    "draft_year": 2009,
                    "draft_round": 1,
                    "draft_number": 7,
                    "team": {
                      "id": 10,
                      "abbreviation": "GSW",
                      "city": "Golden State",
                      "conference": "West",
                      "division": "Pacific",
                      "full_name": "Golden State Warriors",
                      "name": "Warriors"
                    }
                  }
                }
                """.trimIndent()
            server.enqueue(MockResponse().setBody(json))

            val response = api.getPlayer(19)

            assertEquals("Stephen", response.data.firstName)
            assertEquals("GSW", response.data.team.abbreviation)
            assertEquals("6-2", response.data.height)
            assertEquals("185", response.data.weight)
            assertEquals("30", response.data.jerseyNumber)
            assertEquals("Davidson", response.data.college)
            assertEquals("USA", response.data.country)
            assertEquals(2009, response.data.draftYear)
            assertEquals(1, response.data.draftRound)
            assertEquals(7, response.data.draftNumber)
        }

    @Test
    fun `getTeam parses single team wrapped in data envelope`() =
        runTest {
            val json =
                """
                {
                  "data": {
                    "id": 10,
                    "abbreviation": "GSW",
                    "city": "Golden State",
                    "conference": "West",
                    "division": "Pacific",
                    "full_name": "Golden State Warriors",
                    "name": "Warriors"
                  }
                }
                """.trimIndent()
            server.enqueue(MockResponse().setBody(json))

            val response = api.getTeam(10)

            assertEquals("Golden State Warriors", response.data.fullName)
        }

    @Test
    fun `getTeams returns success`() =
        runTest {
            val json =
                """
                {
                  "data": [
                    {
                      "id": 14,
                      "abbreviation": "LAL",
                      "city": "Los Angeles",
                      "conference": "West",
                      "division": "Pacific",
                      "full_name": "Los Angeles Lakers",
                      "name": "Lakers"
                    }
                  ]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setBody(json))

            val response = api.getTeams()

            assertEquals(1, response.data.size)
            assertEquals("LAL", response.data[0].abbreviation)
        }
}
