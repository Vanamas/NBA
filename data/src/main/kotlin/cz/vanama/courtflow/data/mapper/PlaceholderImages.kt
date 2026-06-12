package cz.vanama.courtflow.data.mapper

/**
 * Builds URLs of generated placeholder images served by DiceBear
 * (https://www.dicebear.com — free, no API key).
 *
 * The balldontlie API does not provide any photos, so the screens use
 * generated illustrations instead. The entity id is used as the generation
 * seed, which makes the image deterministic: the same player or team always
 * gets the same picture.
 *
 * DiceBear replaced pollinations.ai, which started returning HTTP 402
 * (payment required) for anonymous image generation in 2026.
 */
internal object PlaceholderImages {
    private const val BASE_URL = "https://api.dicebear.com/9.x"

    /** Returns a deterministic avatar URL for the given player. */
    fun playerPortrait(
        playerId: Int,
        size: Int = 512,
    ): String = "$BASE_URL/avataaars/png?seed=$playerId&size=$size"

    /** Returns a deterministic geometric emblem URL for the given team. */
    fun teamEmblem(
        teamId: Int,
        size: Int = 512,
    ): String = "$BASE_URL/shapes/png?seed=$teamId&size=$size"
}
