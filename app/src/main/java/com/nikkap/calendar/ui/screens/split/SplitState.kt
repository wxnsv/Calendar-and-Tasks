package com.nikkap.calendar.ui.screens.split

import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import java.time.LocalDate

data class SplitState(
    val items: List<SplitEntity> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false
)
