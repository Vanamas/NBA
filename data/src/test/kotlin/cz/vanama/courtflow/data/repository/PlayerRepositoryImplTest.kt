package cz.vanama.courtflow.data.repository

import cz.vanama.courtflow.core.network.generated.api.NBAApi
import cz.vanama.courtflow.core.network.generated.model.NBAPlayer
import cz.vanama.courtflow.core.network.generated.model.NBATeam
import cz.vanama.courtflow.core.network.generated.model.NbaV1PlayersIdGet200Response
import cz.vanama.courtflow.domain.error.DataErrorKind
import cz.vanama.courtflow.domain.error.DataException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class PlayerRepositoryImplTest {
    private lateinit var api: NBAApi
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = PlayerRepositoryImpl(api)
    }

    @Test
    fun `getPlayerById returns the mapped player`() =
        runTest {
            coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = curryDto)

            val result = repository.getPlayerById(19)

            assertEquals(19, result.id)
            assertEquals("Stephen", result.firstName)
            assertEquals("Curry", result.lastName)
            assertEquals("G", result.position)
            assertEquals("30", result.jerseyNumber)
            assertEquals("Golden State Warriors", result.team.fullName)
        }

    @Test
    fun `getPlayerById translates a missing player payload into UNKNOWN`() {
        coEvery { api.nbaV1PlayersIdGet(19) } returns NbaV1PlayersIdGet200Response(data = null)

        val e =
            assertThrows(DataException::class.java) {
                runBlocking { repository.getPlayerById(19) }
            }

        assertEquals(DataErrorKind.UNKNOWN, e.kind)
    }

    @Test
    fun `getPlayerById translates HTTP 404 into NOT_FOUND`() =
        expectGetPlayerByIdKind(httpException(404), DataErrorKind.NOT_FOUND)

    @Test
    fun `getPlayerById translates HTTP 500 into SERVER`() =
        expectGetPlayerByIdKind(httpException(500), DataErrorKind.SERVER)

    @Test
    fun `getPlayerById translates IOException into NETWORK`() =
        expectGetPlayerByIdKind(IOException("offline"), DataErrorKind.NETWORK)

    private fun expectGetPlayerByIdKind(
        thrown: Exception,
        expected: DataErrorKind,
    ) {
        coEvery { api.nbaV1PlayersIdGet(19) } throws thrown

        val e =
            assertThrows(DataException::class.java) {
                runBlocking { repository.getPlayerById(19) }
            }

        assertEquals(expected, e.kind)
    }

    private fun httpException(code: Int): HttpException = HttpException(Response.error<Any>(code, "".toResponseBody()))

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

    private val curryDto =
        NBAPlayer(
            id = 19,
            firstName = "Stephen",
            lastName = "Curry",
            position = "G",
            height = "6-2",
            weight = "185",
            jerseyNumber = "30",
            college = "Davidson",
            country = "USA",
            draftYear = 2009,
            draftRound = 1,
            draftNumber = 7,
            team = warriorsDto,
        )
}
