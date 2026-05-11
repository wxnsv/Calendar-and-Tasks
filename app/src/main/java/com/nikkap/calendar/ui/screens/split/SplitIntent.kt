package com.nikkap.calendar.ui.screens.split

import java.time.LocalDate

sealed interface SplitIntent {
    data class UpdateSelectedDate(val date: LocalDate?) : SplitIntent
}