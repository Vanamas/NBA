package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Card with the player's portrait, name, position and current team,
 * used as a single item of the player list.
 *
 * @param imageUrl URL of the player portrait loaded by Glide.
 * @param onClick called when the user taps the card.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerCard(
    firstName: String,
    lastName: String,
    position: String,
    teamName: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlideImage(
                model = imageUrl,
                contentDescription = "$firstName $lastName",
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = position,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerCardPreview() {
    CourtFlowTheme(dynamicColor = false) {
        PlayerCard(
            firstName = "Stephen",
            lastName = "Curry",
            position = "G",
            teamName = "Golden State Warriors",
            imageUrl = "",
            onClick = {},
        )
    }
}
