package cz.vanama.courtflow.core.designsystem.util

/**
 * Builds URLs of generated placeholder images served by pollinations.ai.
 *
 * The balldontlie API does not provide any photos, so detail screens use
 * generated illustrations instead. The entity id is used as the generation
 * seed, which makes the image deterministic: the same player or team always
 * gets the same picture.
 */
object PlaceholderImages {
    private const val BASE_URL = "https://image.pollinations.ai/prompt"

    /** URL-encoded prompt: "basketball player portrait, flat vector illustration, orange jersey" */
    private const val PLAYER_PROMPT =
        "basketball%20player%20portrait%2C%20flat%20vector%20illustration%2C%20orange%20jersey"

    /** URL-encoded prompt: "basketball team emblem, flat minimal vector illustration" */
    private const val TEAM_PROMPT =
        "basketball%20team%20emblem%2C%20flat%20minimal%20vector%20illustration"

    /** Returns a deterministic portrait illustration URL for the given player. */
    fun playerPortrait(
        playerId: Int,
        size: Int = 512,
    ): String = "$BASE_URL/$PLAYER_PROMPT?width=$size&height=$size&seed=$playerId&model=flux&nologo=true"

    /** Returns a deterministic emblem illustration URL for the given team. */
    fun teamEmblem(
        teamId: Int,
        size: Int = 512,
    ): String = "$BASE_URL/$TEAM_PROMPT?width=$size&height=$size&seed=$teamId&model=flux&nologo=true"
}
