package cz.vanama.courtflow.core.network.generated.api

import cz.vanama.courtflow.core.network.di.NetworkMoshi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Verifies that the [NBAApi] client generated from the official OpenAPI
 * definition (`openapi/nba.yml`) parses the real balldontlie response
 * envelopes and addresses the `nba/v1` endpoints relative to the bare host.
 */
class GeneratedApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var api: NBAApi

    @Before
    fun setup() {
        server = MockWebServer()
        val moshi = NetworkMoshi.create()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NBAApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getPlayers parses the list envelope and paging metadata`() =
        runTest {
            server.enqueue(MockResponse().setBody(PLAYERS_PAGE_JSON))

            val response = api.nbaV1PlayersGet()

            assertEquals(1, response.data?.size)
            assertEquals("LeBron", response.data?.first()?.firstName)
            assertEquals(2, response.meta?.nextCursor)
        }

    @Test
    fun `getPlayers sends team_ids array parameter`() =
        runTest {
            val json = """{"data": [], "meta": {"per_page": 35}}"""
            server.enqueue(MockResponse().setBody(json))

            api.nbaV1PlayersGet(teamIds = listOf(10))

            val request = server.takeRequest()
            assertEquals(listOf("10"), request.requestUrl?.queryParameterValues("team_ids[]"))
        }

    @Test
    fun `getPlayers addresses the versioned endpoint with query parameters`() =
        runTest {
            server.enqueue(MockResponse().setBody(PLAYERS_PAGE_JSON))

            api.nbaV1PlayersGet(cursor = 2, perPage = 35)

            assertEquals("/nba/v1/players?cursor=2&per_page=35", server.takeRequest().path)
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

            val player = api.nbaV1PlayersIdGet(19).data

            assertEquals("Stephen", player?.firstName)
            assertEquals("GSW", player?.team?.abbreviation)
            assertEquals("6-2", player?.height)
            assertEquals("185", player?.weight)
            assertEquals("30", player?.jerseyNumber)
            assertEquals("Davidson", player?.college)
            assertEquals("USA", player?.country)
            assertEquals(2009, player?.draftYear)
            assertEquals(1, player?.draftRound)
            assertEquals(7, player?.draftNumber)
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

            val response = api.nbaV1TeamsIdGet(10)

            assertEquals("Golden State Warriors", response.data?.fullName)
        }

    @Test
    fun `getTeams parses the list envelope`() =
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

            val response = api.nbaV1TeamsGet()

            assertEquals(1, response.data?.size)
            assertEquals("LAL", response.data?.first()?.abbreviation)
        }

    @Test
    fun `getTeams tolerates historical teams with blank conference and division`() =
        runTest {
            // balldontlie's /teams returns BAA-era teams whose conference is "    "
            // (whitespace) and division is "" — neither a valid enum value. The whole
            // page must still parse, with those fields falling back to null.
            val json =
                """
                {
                  "data": [
                    {
                      "id": 1, "abbreviation": "ATL", "city": "Atlanta",
                      "conference": "East", "division": "Southeast",
                      "full_name": "Atlanta Hawks", "name": "Hawks"
                    },
                    {
                      "id": 37, "abbreviation": "CHS", "city": "",
                      "conference": "    ", "division": "",
                      "full_name": "Chicago Stags", "name": "Chicago Stags"
                    }
                  ]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setBody(json))

            val response = api.nbaV1TeamsGet()

            assertEquals(2, response.data?.size)
            val historical = response.data?.last()
            assertEquals("Chicago Stags", historical?.fullName)
            assertNull(historical?.conference)
            assertNull(historical?.division)
        }

    private companion object {
        val PLAYERS_PAGE_JSON =
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
    }
}
