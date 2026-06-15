package cz.vanama.courtflow.domain.repository

import cz.vanama.courtflow.domain.model.Standing

/**
 * Access to NBA standings; implemented in the data layer on top of the
 * remote API.
 */
interface StandingsRepository {
    /**
     * Returns the current-season standing of the team with [teamId], or
     * `null` when the team has no published standing yet (e.g. off-season).
     */
    suspend fun getTeamStanding(teamId: Int): Standing?
}
