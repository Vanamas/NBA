package cz.vanama.courtflow.navigation

import android.net.Uri

/**
 * Mapping between `courtflow://` URIs and [Destination]s. The same link
 * format is produced by the share actions on the detail screens.
 */
internal object DeepLink {
    private const val SCHEME = "courtflow"
    private const val HOST_PLAYER = "player"
    private const val HOST_TEAM = "team"

    /** Returns the destination for [uri], or `null` when it is not a recognized deep link. */
    fun parse(uri: Uri?): Destination? {
        if (uri == null || uri.scheme != SCHEME) return null
        val id = uri.lastPathSegment?.toIntOrNull() ?: return null
        return when (uri.host) {
            HOST_PLAYER -> Destination.PlayerDetail(id)
            HOST_TEAM -> Destination.TeamDetail(id)
            else -> null
        }
    }
}
