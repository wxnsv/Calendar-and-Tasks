package com.nikkap.calendar.ui.screens.split.utils.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.nikkap.calendar.core.utils.toColor
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Preview
@Composable
private fun Preview() {
    val list = listOf(
        SplitEntity.TaskItem(
            Task(
                id = "1",
                title = "Task",
                deadline = 1777075200000
            )
        ),
        SplitEntity.EventItem(
            Event(
                id = "2",
                summary = "Event",
                startTimestamp = 1777075200000
            )
        ),
        SplitEntity.BirthdayItem(
            Birthday(
                id = "3",
                name = "Birthday",
                date = 1777075200000
            )
        )
    )
    Calendar(list)
}

@Composable
fun Calendar(listOfItems: List<SplitEntity>) {
    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(26) }
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
        outDateStyle = OutDateStyle.EndOfGrid
    )
    val groupedListEntitiesByDate: List<List<SplitEntity>> = listOfItems
        .groupBy { it.date.toLocalDate() }
        .values
        .toList()
        .sortedBy { it[0].date.toLocalDate() }
    val colorsByDayMap = listOfItems.filterNot {
        it is SplitEntity.SubtaskItem
    }.groupBy(
        keySelector = { it.date.toLocalDate() },
        valueTransform = { it.colorHex.toColor() }
    )
    HorizontalCalendar(
        modifier = Modifier
            .wrapContentWidth()
            .background(Color.Black),
        state = state,
        monthHeader = {
            WeekDaysTitle(daysOfWeek = daysOfWeek)
        },
        dayContent = { day ->
            Day(
                day,
                onClick = { },
                isSelected = selectedDate == day.date,
                colorsList = colorsByDayMap[day.date] ?: emptyList()
            )


        },
    )
}