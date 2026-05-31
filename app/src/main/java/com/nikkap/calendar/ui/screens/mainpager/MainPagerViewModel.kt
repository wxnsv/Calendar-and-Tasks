package com.nikkap.calendar.ui.screens.mainpager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainPagerViewModel(
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainPagerState())
    val state: StateFlow<MainPagerState> =
        _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainPagerState()
    )

    fun toggleMenu() {
        _state.update { it.copy(isMenuExpanded = !it.isMenuExpanded) }
    }

    fun switchScreen(pos: Int) {
        viewModelScope.launch {
            val isListScreen = pos == 0
            if (userPrefRepository.userStateFlow.first().isLastOpenedSelected) {
                userPrefRepository.updateIsListScreenLast(isListScreen)
            }
        }
    }

}