package com.nikkap.calendar.ui.screens.create

import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.TaskList

sealed interface CreateIntent {
    data class UpdateTitle(val title: String) : CreateIntent

    data class UpdateShowFragment(val type: CalendarEntry) : CreateIntent

    data class UpdateItem(val type: String, val id: String) : CreateIntent
}

sealed interface CreateTaskIntent {
    data class UpdateDescription(val description: String) : CreateTaskIntent
    data class UpdateList(val taskList: TaskList) : CreateTaskIntent
    data class UpdateRepeat(val repeat: String) : CreateTaskIntent
    data class UpdateDeadline(val deadline: Long) : CreateTaskIntent
    object SaveTask : CreateTaskIntent
}

sealed interface CreateEventIntent {
    data class UpdateIsAllDay(val isAllDay: Boolean) : CreateEventIntent
    data class UpdateColor(val color: String?) : CreateEventIntent
    data class UpdateDescription(val description: String) : CreateEventIntent
    data class UpdateStartDate(val startDate: Long) : CreateEventIntent
    data class UpdateStartTime(val startTime: Long) : CreateEventIntent
    data class UpdateEndDate(val endDate: Long) : CreateEventIntent
    data class UpdateEndTime(val endTime: Long) : CreateEventIntent
    object SaveEvent : CreateEventIntent

}

sealed interface CreateBirthdayIntent {
    data class UpdateColor(val color: String) : CreateBirthdayIntent
    data class UpdateDate(val date: Long) : CreateBirthdayIntent
    object SaveBirthday : CreateBirthdayIntent
}