package cz.vanama.courtflow.navigation

import android.net.Uri

/**
 * Mapping between `courtflow://` URIs and [Destination]s. The same link
 * format is produced by the share actions on the detail screens, and the
 * list URIs back the static launcher shortcuts (`res/xml/shortcuts.xml`).
 */
internal object DeepLink {
    private const val SCHEME = "courtflow"
    private const val HOST_PLAYER = "player"
    private const val HOST_TEAM = "team"
    private const val HOST_PLAYERS = "players"
    private const val HOST_TEAMS = "teams"

    /** Returns the destination for [uri], or `null` when it is not a recognized deep link. */
    fun parse(uri: Uri?): Destination? {
        if (uri == null || uri.scheme != SCHEME) return null
        val id = uri.lastPathSegment?.toIntOrNull()
        return when {
            uri.host == HOST_PLAYERS && uri.pathSegments.isEmpty() -> Destination.PlayerList
            uri.host == HOST_TEAMS && uri.pathSegments.isEmpty() -> Destination.TeamList
            id == null -> null
            uri.host == HOST_PLAYER -> Destination.PlayerDetail(id)
            uri.host == HOST_TEAM -> Destination.TeamDetail(id)
            else -> null
        }
    }

    /**
     * Initial Navigation 3 back stack for [uri]: the player list root plus the
     * deep-linked destination on top, so system back from a deep-linked screen
     * lands on the list. A `courtflow://players` link resolves to the root
     * itself and must not duplicate it.
     */
    fun initialBackStack(uri: Uri?): List<Destination> {
        val target = parse(uri)
        return listOfNotNull(Destination.PlayerList, target.takeUnless { it == Destination.PlayerList })
    }
}
