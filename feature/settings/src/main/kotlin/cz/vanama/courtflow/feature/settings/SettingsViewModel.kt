package cz.vanama.courtflow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.vanama.courtflow.core.common.settings.ThemeMode
import cz.vanama.courtflow.core.common.settings.ThemePreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel of the settings screen; reflects the persisted theme
 * preferences into [uiState] and writes every change straight back through the
 * repository (the persisted store is the single source of truth).
 */
class SettingsViewModel(
    private val repository: ThemePreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsState>
        field = MutableStateFlow(SettingsState())

    init {
        viewModelScope.launch {
            repository.themePreferences.collect { prefs ->
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
            is SettingsIntent.OnDynamicColorChanged -> setDynamicColor(intent.enabled)
            is SettingsIntent.OnThemeModeChanged -> setThemeMode(intent.mode)
            is SettingsIntent.OnTrueBlackChanged -> setTrueBlack(intent.enabled)
        }
    }

    private fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }

    private fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    private fun setTrueBlack(enabled: Boolean) {
        viewModelScope.launch { repository.setTrueBlack(enabled) }
    }
}
