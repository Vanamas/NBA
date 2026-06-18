package cz.vanama.courtflow.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.designsystem.theme.CourtFlowTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale

/**
 * Public, stateful entry point: wires the [SettingsViewModel], collects its
 * one-shot effects (a snackbar when a preference fails to persist) and renders
 * the stateless screen. Language selection now flows through the ViewModel like
 * every other control, so this composable no longer touches AppCompat directly.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = koinViewModel()
    val appInfoProvider: AppInfoProvider = koinInject()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val writeErrorMessage = stringResource(R.string.settings_write_error)

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    SettingsEffect.PreferenceWriteFailed -> snackbarHostState.showSnackbar(writeErrorMessage)
                    SettingsEffect.OpenOssLicenses -> appInfoProvider.openOssLicenses()
                }
            }
        }
    }

    SettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/** Internal, stateless overload: Scaffold + top bar + snackbar host. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SettingsContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.padding(padding),
        )
    }
}

/** Internal, stateless content: pure state + a single intent sink. UI tests target this. */
@Composable
internal fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        ThemePreviewCard()
        AppearanceSection(state = state, onIntent = onIntent)
        SectionHeader(stringResource(R.string.settings_theme_section))
        ThemeModeSection(selected = state.themeMode, onIntent = onIntent)
        SectionHeader(stringResource(R.string.settings_language_section))
        LanguageSection(
            selectedTag = state.currentLanguageTag,
            tags = state.languageTags,
            onSelect = { onIntent(SettingsIntent.OnLanguageSelected(it)) },
        )
        AboutSection(
            versionName = state.versionName,
            onOssLicensesClick = { onIntent(SettingsIntent.OnOssLicensesClicked) },
        )
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

/**
 * Language options derived from the app's declared locales (see
 * [AppLocaleController.supportedLanguageTags]), plus a "follow system" row.
 * Each label is the language's own endonym, so the list stays correct as new
 * translations are added without touching this screen.
 */
@Composable
private fun LanguageSection(
    selectedTag: String,
    tags: List<String>,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        RadioRow(stringResource(R.string.settings_language_system), selectedTag.isEmpty()) {
            onSelect("")
        }
        tags.forEach { tag ->
            RadioRow(languageLabel(tag), selectedTag == tag) { onSelect(tag) }
        }
    }
}

@Composable
private fun AboutSection(
    versionName: String,
    onOssLicensesClick: () -> Unit,
) {
    SectionHeader(stringResource(R.string.settings_about_section))
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_about_version)) },
        trailingContent = { Text(versionName) },
    )
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_about_licenses)) },
        supportingContent = { Text(stringResource(R.string.settings_about_licenses_summary)) },
        modifier = Modifier.clickable(onClick = onOssLicensesClick),
    )
}

private fun languageLabel(tag: String): String {
    val locale = Locale.forLanguageTag(tag)
    return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    CourtFlowTheme {
        SettingsScreen(
            state = SettingsState(languageTags = listOf("en", "cs")),
            onIntent = {},
        )
    }
}
