package com.nikkap.calendar.app.ui.screens.split

import java.time.LocalDate

sealed interface SplitIntent {
    data class UpdateSelectedDate(val date: LocalDate?) : SplitIntent
    data class PendingDeleteItem(val id: String, val type: String) : SplitIntent
    data class UndoPendingDelete(val id: String) : SplitIntent
}