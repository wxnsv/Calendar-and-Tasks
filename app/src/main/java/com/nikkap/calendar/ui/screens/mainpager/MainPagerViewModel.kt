package com.nikkap.calendar.ui.screens.mainpager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainPagerViewModel(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {
    private val _isListScreenLast = userPrefRepository.isListScreenLast
    private val _state = MutableStateFlow(MainPagerState())
    val state: StateFlow<MainPagerState> = combine(
        _state,
        _isListScreenLast
    ) { state, isListScreenLast ->

        state.copy(
            isListScreen = isListScreenLast
        )
    }.stateIn(
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
            _state.update { it.copy(isListScreen = isListScreen) }
            userPrefRepository.updateIsListScreenLast(isListScreen)
        }
    }

}