package cz.vanama.courtflow.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Public, stateful entry point: wires the [SettingsViewModel] and bridges the
 * language picker to AppCompat's per-app locales (the system source of truth
 * for the chosen language).
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTag =
        AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')
    SettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        currentLanguageTag = currentTag,
        onLanguageSelected = { tag ->
            AppCompatDelegate.setApplicationLocales(
                if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
            )
        },
        modifier = modifier,
    )
}

/** Internal, stateless overload: Scaffold + top bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        SettingsContent(
            state = state,
            onIntent = onIntent,
            currentLanguageTag = currentLanguageTag,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier.padding(padding),
        )
    }
}

/** Internal, stateless content: pure state + callbacks. UI tests target this. */
@Composable
internal fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        AppearanceSection(state = state, onIntent = onIntent)
        SectionHeader(stringResource(R.string.settings_theme_section))
        ThemeModeSection(selected = state.themeMode, onIntent = onIntent)
        SectionHeader(stringResource(R.string.settings_language_section))
        LanguageSection(selectedTag = currentLanguageTag, onLanguageSelected = onLanguageSelected)
    }
}

@Composable
private fun AppearanceSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    SwitchRow(
        title = stringResource(R.string.settings_dynamic_color),
        summary = stringResource(R.string.settings_dynamic_color_summary),
        checked = state.dynamicColor,
        onCheckedChange = { onIntent(SettingsIntent.OnDynamicColorChanged(it)) },
    )
    SwitchRow(
        title = stringResource(R.string.settings_true_black),
        summary = stringResource(R.string.settings_true_black_summary),
        checked = state.trueBlack,
        onCheckedChange = { onIntent(SettingsIntent.OnTrueBlackChanged(it)) },
    )
}

@Composable
private fun ThemeModeSection(
    selected: ThemeMode,
    onIntent: (SettingsIntent) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        RadioRow(stringResource(R.string.settings_theme_light), selected == ThemeMode.LIGHT) {
            onIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.LIGHT))
        }
        RadioRow(stringResource(R.string.settings_theme_dark), selected == ThemeMode.DARK) {
            onIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.DARK))
        }
        RadioRow(stringResource(R.string.settings_theme_system), selected == ThemeMode.SYSTEM) {
            onIntent(SettingsIntent.OnThemeModeChanged(ThemeMode.SYSTEM))
        }
    }
}

@Composable
private fun LanguageSection(
    selectedTag: String,
    onLanguageSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        RadioRow(stringResource(R.string.settings_language_system), selectedTag.isEmpty()) {
            onLanguageSelected("")
        }
        RadioRow(stringResource(R.string.settings_language_en), selectedTag == "en") {
            onLanguageSelected("en")
        }
        RadioRow(stringResource(R.string.settings_language_cs), selectedTag == "cs") {
            onLanguageSelected("cs")
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier =
            Modifier.toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            ),
    )
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier =
            Modifier.selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    CourtFlowTheme {
        SettingsScreen(
            state = SettingsState(),
            onIntent = {},
            currentLanguageTag = "",
            onLanguageSelected = {},
        )
    }
}
