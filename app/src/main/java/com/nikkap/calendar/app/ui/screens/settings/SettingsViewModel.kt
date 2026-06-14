package com.nikkap.calendar.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    private val _userPrefs = userPrefRepository.userStateFlow

    val state = combine(
        _state,
        _userPrefs
    ) { state, userPrefs ->
        state.copy(
            userName = userPrefs.name ?: "Not authorized",
            userEmail = userPrefs.email ?: "",
            isListStartScreen = if (userPrefs.isLastOpenedSelected) null else userPrefs.isListScreenLast,
            isLightTheme = if (userPrefs.isSystemTheme) null else userPrefs.isLightTheme,
            userImagePath = userPrefs.photoPath ?: "",
            isMondayFirstDayOfWeek = if (userPrefs.isSystemFirstDay) null else userPrefs.isMondayFirstDay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateFirstDayOfWeek -> {
                viewModelScope.launch {
                    if (intent.isMonday == null) {
                        userPrefRepository.updateIsSystemFirstDay(true)
                    } else userPrefRepository.updateIsMondayFirstDay(intent.isMonday)
                }
                _state.update {
                    it.copy(
                        isMondayFirstDayOfWeek = intent.isMonday
                    )
                }
            }

            is SettingsIntent.UpdateStartScreen -> {
                viewModelScope.launch {
                    if (intent.isList == null) {
                        userPrefRepository.updateIsLastOpenedScreen(true)
                    } else {
                        userPrefRepository.updateIsListScreenLast(intent.isList)
                        userPrefRepository.updateIsLastOpenedScreen(false)
                    }
                }
                _state.update {
                    it.copy(isListStartScreen = intent.isList)
                }
            }

            is SettingsIntent.UpdateTheme -> {
                viewModelScope.launch {
                    if (intent.isLight == null) {
                        userPrefRepository.updateIsSystemTheme(true)
                    } else userPrefRepository.updateIsLightTheme(intent.isLight)
                }
                _state.update {
                    it.copy(
                        isLightTheme = intent.isLight
                    )
                }
            }
        }
    }
}