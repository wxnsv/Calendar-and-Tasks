package com.nikkap.calendar.app.ui.screens.split.utils.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.nikkap.calendar.app.ui.screens.split.SplitState
import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import com.nikkap.calendar.app.ui.utils.toColor
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
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
        val listState = rememberLazyListState()
        Calendar(list, listState, SplitState(), {})
    }
}


@Composable
fun Calendar(
    listOfItems: List<SplitEntity>,
    listState: LazyListState,
    state: SplitState,
    onSelectedDateChanged: (LocalDate?) -> Unit
) {
    lateinit var calendarState: CalendarState
    if (listOfItems.isEmpty()) return

    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val firstDay = if (state.isMondayFirst) DayOfWeek.MONDAY else DayOfWeek.SUNDAY

    key(state.isMondayFirst) {
        calendarState = rememberCalendarState(
            startMonth = currentMonth.minusMonths(36),
            endMonth = currentMonth.plusMonths(24),
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = firstDay,
            outDateStyle = OutDateStyle.EndOfGrid
        )
    }

    val daysOfWeek = remember(firstDay) {
        daysOfWeek(firstDayOfWeek = firstDay)
    }
    val nearest = listOfItems.minByOrNull { item ->
        System.currentTimeMillis() - item.date
    }

    val topVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex / 2 }
    }

    val groupedListEntitiesByDate: List<List<SplitEntity>> = listOfItems
        .groupBy { it.date.toLocalDate() }
        .values
        .toList()
        .sortedBy { it[0].date.toLocalDate() }
    if (nearest != null && listOfItems.isNotEmpty()) {
        LaunchedEffect(nearest) {
            coroutineScope.launch {
                listState.scrollToItem(groupedListEntitiesByDate.indexOf(groupedListEntitiesByDate.find {
                    it.contains(
                        nearest
                    )
                }) * 2)
            }
        }
    }
    val listEventMonth = groupedListEntitiesByDate.getOrNull(topVisibleIndex)
        ?.firstOrNull()?.date?.toLocalDate()?.yearMonth
    selectedDate = state.selectedDate
    onSelectedDateChanged(
        groupedListEntitiesByDate.getOrNull(topVisibleIndex)?.first()?.date?.toLocalDate()
    )
    if (listEventMonth != state.selectedDate?.yearMonth && listEventMonth != null) {
        LaunchedEffect(state) {
            coroutineScope.launch {
                calendarState.animateScrollToMonth(
                    listEventMonth
                )
            }
        }
    }


    val colorsByDayMap = listOfItems.filterNot {
        it is SplitEntity.SubtaskItem
    }.groupBy(
        keySelector = { it.date.toLocalDate() },
        valueTransform = { it.colorHex.toColor() }
    )
    CalendarTitle(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 12.dp),
        currentMonth = listEventMonth ?: currentMonth,
    )
    HorizontalCalendar(
        modifier = Modifier
            .wrapContentWidth()
            .background(MaterialTheme.colorScheme.outline),
        state = calendarState,
        monthHeader = {
            WeekDaysTitle(daysOfWeek = daysOfWeek)
        },
        dayContent = { day ->
            Day(
                day,
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            groupedListEntitiesByDate.indexOf(
                                groupedListEntitiesByDate.find { it.first().date.toLocalDate() == day.date }) * 2
                        )
                        onSelectedDateChanged(day.date)
                    }
                },
                isSelected = selectedDate == day.date,
                colorsList = colorsByDayMap[day.date] ?: emptyList()
            )
        },
        userScrollEnabled = false,


    )

}