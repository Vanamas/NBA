package cz.vanama.courtflow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.core.common.settings.ThemePreferencesStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * MVI ViewModel of the settings screen. Reflects the persisted theme preferences
 * into [uiState] and writes every change straight back through the store (the
 * persisted store is the single source of truth). The per-app language is owned
 * here too, applied through [AppLocaleController] so it stays inside the MVI loop.
 */
class SettingsViewModel(
    private val store: ThemePreferencesStore,
    private val localeController: AppLocaleController,
) : ViewModel() {
    val uiState: StateFlow<SettingsState>
        field =
        MutableStateFlow(
            SettingsState(
                currentLanguageTag = localeController.currentLanguageTag(),
                languageTags = localeController.supportedLanguageTags(),
            ),
        )

    val uiEffect: SharedFlow<SettingsEffect>
        field = MutableSharedFlow<SettingsEffect>()

    init {
        viewModelScope.launch {
            store.themePreferences.collect { prefs ->
                uiState.update {
                    it.copy(
                        dynamicColor = prefs.dynamicColor,
                        themeMode = prefs.themeMode,
                        trueBlack = prefs.trueBlack,
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.OnDynamicColorChanged -> persist { store.setDynamicColor(intent.enabled) }
            is SettingsIntent.OnThemeModeChanged -> persist { store.setThemeMode(intent.mode) }
            is SettingsIntent.OnTrueBlackChanged -> persist { store.setTrueBlack(intent.enabled) }
            is SettingsIntent.OnLanguageSelected -> selectLanguage(intent.tag)
            is SettingsIntent.OnOssLicensesClicked -> Unit
        }
    }

    private fun selectLanguage(tag: String) {
        localeController.setLanguage(tag)
        uiState.update { it.copy(currentLanguageTag = tag) }
    }

    private fun persist(write: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                write()
            } catch (_: IOException) {
                // A persisted write failed; surface it so the change isn't silently lost.
                uiEffect.emit(SettingsEffect.PreferenceWriteFailed)
            }
        }
    }
}
