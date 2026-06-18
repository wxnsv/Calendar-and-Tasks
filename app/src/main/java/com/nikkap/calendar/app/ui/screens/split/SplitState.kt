package com.nikkap.calendar.app.ui.screens.split

import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import java.time.LocalDate

data class SplitState(
    val items: List<SplitEntity> = emptyList(),
    val itemsWithoutDate: List<SplitEntity> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val isMondayFirst: Boolean = true
)
