package cz.vanama.courtflow.domain.model

/** Kind of entity that can be favorited; the discriminator shared by the favorites repository. */
enum class FavoriteType {
    PLAYER,
    TEAM,
}
