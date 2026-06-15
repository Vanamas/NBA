package cz.vanama.courtflow.feature.teams.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.domain.model.Game
import cz.vanama.courtflow.feature.teams.R

/** The "Recent games" block; renders nothing when [games] is empty (off-season or failed load). */
internal fun LazyListScope.recentGamesSection(games: List<Game>) {
    if (games.isEmpty()) return
    item { SectionHeader(text = stringResource(R.string.team_detail_recent_games)) }
    items(games, key = { it.id }) { game ->
        RecentGameRow(game = game)
    }
}

/** One compact score line: game date, home abbreviation, score, visitor abbreviation. */
@Composable
private fun RecentGameRow(
    game: Game,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Text(
            text = game.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = game.homeTeam.abbreviation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.game_score, game.homeTeamScore, game.visitorTeamScore),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = game.visitorTeam.abbreviation,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
