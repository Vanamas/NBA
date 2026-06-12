package cz.vanama.courtflow.core.network.api

import cz.vanama.courtflow.core.network.model.CommonResponse
import cz.vanama.courtflow.core.network.model.PlayerDto
import cz.vanama.courtflow.core.network.model.SingleResponse
import cz.vanama.courtflow.core.network.model.TeamDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit definition of the balldontlie REST API (`https://api.balldontlie.io/v1/`).
 *
 * List endpoints return a [CommonResponse] with paging metadata, single-resource
 * endpoints wrap the entity in a [SingleResponse] envelope.
 */
interface BallDontLieApi {
    /**
     * Returns one page of players.
     *
     * @param cursor cursor of the next page from the previous response, `null` for the first page.
     * @param perPage page size, the API caps it at 100.
     * @param search optional name filter applied by the API (matches first/last name).
     */
    @GET("players")
    suspend fun getPlayers(
        @Query("cursor") cursor: Int? = null,
        @Query("per_page") perPage: Int = 35,
        @Query("search") search: String? = null,
    ): CommonResponse<PlayerDto>

    /** Returns a single player by [id]. */
    @GET("players/{id}")
    suspend fun getPlayer(
        @Path("id") id: Int,
    ): SingleResponse<PlayerDto>

    /** Returns all NBA teams. */
    @GET("teams")
    suspend fun getTeams(): CommonResponse<TeamDto>

    /** Returns a single team by [id]. */
    @GET("teams/{id}")
    suspend fun getTeam(
        @Path("id") id: Int,
    ): SingleResponse<TeamDto>
}
