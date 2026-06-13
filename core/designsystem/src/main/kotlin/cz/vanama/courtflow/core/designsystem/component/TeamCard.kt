package cz.vanama.courtflow.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.core.designsystem.R
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme

/**
 * Card with the team's abbreviation badge, full name and its
 * conference/division, used as a single item of the team list.
 *
 * @param abbreviation short team code (e.g. `GSW`) rendered as a leading
 *   badge; skipped when blank.
 * @param onClick called when the user taps the card.
 */
@Composable
fun TeamCard(
    fullName: String,
    conference: String,
    division: String,
    abbreviation: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (abbreviation.isNotBlank()) {
                Badge(
                    text = abbreviation,
                    tone = BadgeTone.Primary,
                    textStyle = MaterialTheme.typography.labelMedium,
                    minHeight = 26.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.team_card_conference_division, conference, division),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TeamCardPreview() {
    CourtFlowTheme {
        TeamCard(
            fullName = "Golden State Warriors",
            conference = "West",
            division = "Pacific",
            abbreviation = "GSW",
            onClick = {},
        )
    }
}
