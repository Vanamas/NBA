package cz.vanama.courtflow.feature.players.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Horizontal filter bar above the player grid: a team-picker chip plus one
 * toggleable [FilterChip] per position. Re-tapping the active position clears
 * it; the team chip opens a [TeamPickerSheet].
 */
@Composable
internal fun PlayerFilterBar(
    teams: List<Team>,
    selectedTeam: Team?,
    selectedPosition: String?,
    onTeamSelected: (Team?) -> Unit,
    onPositionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTeamPicker by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        FilterChip(
            selected = selectedTeam != null,
            onClick = { showTeamPicker = true },
            label = {
                Text(selectedTeam?.name ?: stringResource(R.string.player_filter_team))
            },
        )
        POSITION_OPTIONS.forEach { (code, labelRes) ->
            FilterChip(
                selected = selectedPosition == code,
                onClick = {
                    onPositionSelected(if (selectedPosition == code) null else code)
                },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
    if (showTeamPicker) {
        TeamPickerSheet(
            teams = teams,
            onTeamSelected = { team ->
                onTeamSelected(team)
                showTeamPicker = false
            },
            onDismiss = { showTeamPicker = false },
        )
    }
}

/**
 * Modal bottom sheet listing all [teams] plus an "All teams" entry; tapping a
 * row reports the chosen team (or `null` for "All teams").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamPickerSheet(
    teams: List<Team>,
    onTeamSelected: (Team?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.player_filter_all_teams),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickableRow { onTeamSelected(null) },
                )
            }
            items(teams) { team ->
                Text(
                    text = team.fullName,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickableRow { onTeamSelected(team) },
                )
            }
        }
    }
}

/** Shared 16dp-padded clickable modifier for the picker rows. */
private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this.then(rowClickable(onClick))

/** A row-height clickable + padding modifier extracted to keep callers short. */
private fun rowClickable(onClick: () -> Unit): Modifier =
    Modifier
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp)
