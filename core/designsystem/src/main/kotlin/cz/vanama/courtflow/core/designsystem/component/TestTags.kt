package cz.vanama.courtflow.core.designsystem.component

/**
 * Test tags shared by design-system components and the feature screens that
 * reuse the same UI patterns (e.g. the centered loading indicator), so UI
 * tests target one constant instead of repeating raw strings per module.
 */
object TestTags {
    const val LOADING_INDICATOR = "loading_indicator"
    const val AVATAR_LOADING = "avatar_loading"
    const val AVATAR_FAILURE = "avatar_failure"
    const val PLAYER_CARD_SKELETON = "player_card_skeleton"
    const val TEAM_CARD_SKELETON = "team_card_skeleton"
    const val CONNECTIVITY_BANNER = "connectivity_banner"
}
