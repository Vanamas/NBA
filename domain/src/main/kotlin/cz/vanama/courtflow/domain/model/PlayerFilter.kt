package cz.vanama.courtflow.domain.model

/**
 * The active filter of the player list: a free-text [query], an optional NBA
 * [teamId] (`team_ids[]` server filter) and an optional [position] code
 * (`G`, `F`, `C`, `G-F`, `F-C`) applied client-side. An empty filter means
 * "all players" and is served from the offline-first cache.
 */
data class PlayerFilter(
    val query: String = "",
    val teamId: Int? = null,
    val position: String? = null,
) {
    /** True when no search, team or position narrows the catalog. */
    fun isEmpty(): Boolean = query.isBlank() && teamId == null && position == null
}
