package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AttributeRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows the label and the value`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                AttributeRow(label = "Height", value = "6-2")
            }
        }

        composeTestRule.onNodeWithText("Height").assertIsDisplayed()
        composeTestRule.onNodeWithText("6-2").assertIsDisplayed()
    }

    @Test
    fun `renders nothing when the value is null`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                AttributeRow(label = "College", value = null)
            }
        }

        composeTestRule.onNodeWithText("College").assertDoesNotExist()
    }

    @Test
    fun `renders nothing when the value is blank`() {
        composeTestRule.setContent {
            CourtFlowTheme {
                AttributeRow(label = "College", value = "   ")
            }
        }

        composeTestRule.onNodeWithText("College").assertDoesNotExist()
    }

    @Test
    fun `long value wraps within the row instead of overflowing`() {
        val longValue = "2015, Round 1, Pick 30 — an exceedingly long draft description"

        composeTestRule.setContent {
            CourtFlowTheme {
                Box(modifier = Modifier.width(250.dp)) {
                    AttributeRow(label = "Draft", value = longValue)
                }
            }
        }

        val rootRight = composeTestRule.onRoot().getUnclippedBoundsInRoot().right
        val valueRight = composeTestRule.onNodeWithText(longValue).getUnclippedBoundsInRoot().right
        valueRight shouldBeLessThanOrEqualTo rootRight
    }
}
