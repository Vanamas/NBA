package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.vanama.courtflow.domain.model.Team
import cz.vanama.courtflow.feature.players.R

/** Position codes offered by the filter, paired with their label resource. */
private val POSITION_OPTIONS =
    listOf(
        "G" to R.string.player_filter_position_guard,
        "F" to R.string.player_filter_position_forward,
        "C" to R.string.player_filter_position_center,
    )

/**
 * Modal bottom sheet holding the whole player filter — toggleable position
 * chips and a horizontally scrolling team picker — opened from the list's
 * top-bar filter action. Re-tapping an active chip clears it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerFilterSheet(
    teams: List<Team>,
    selectedTeam: Team?,
    selectedPosition: String?,
    onTeamSelected: (Team?) -> Unit,
    onPositionSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        PlayerFilterSheetContent(
            teams = teams,
            selectedTeam = selectedTeam,
            selectedPosition = selectedPosition,
            onTeamSelected = onTeamSelected,
            onPositionSelected = onPositionSelected,
        )
    }
}

/** Stateless content of [PlayerFilterSheet]; rendered directly by previews and UI tests. */
@Composable
internal fun PlayerFilterSheetContent(
    teams: List<Team>,
    selectedTeam: Team?,
    selectedPosition: String?,
    onTeamSelected: (Team?) -> Unit,
    onPositionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.player_filter_title),
            style = MaterialTheme.typography.titleLarge,
        )
        FilterSection(label = stringResource(R.string.player_filter_position_label)) {
            POSITION_OPTIONS.forEach { (code, labelRes) ->
                FilterChip(
                    selected = selectedPosition == code,
                    onClick = { onPositionSelected(if (selectedPosition == code) null else code) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
        FilterSection(label = stringResource(R.string.player_filter_team)) {
            FilterChip(
                selected = selectedTeam == null,
                onClick = { onTeamSelected(null) },
                label = { Text(stringResource(R.string.player_filter_all_teams)) },
            )
            teams.forEach { team ->
                FilterChip(
                    selected = selectedTeam?.id == team.id,
                    onClick = { onTeamSelected(team) },
                    label = { Text(team.abbreviation) },
                )
            }
        }
        TextButton(
            onClick = {
                onPositionSelected(null)
                onTeamSelected(null)
            },
            enabled = selectedTeam != null || selectedPosition != null,
        ) {
            Text(stringResource(R.string.player_filter_clear))
        }
    }
}

/** A labelled row of horizontally scrolling [FilterChip]s. */
@Composable
private fun FilterSection(
    label: String,
    modifier: Modifier = Modifier,
    chips: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
        ) {
            chips()
        }
    }
}
