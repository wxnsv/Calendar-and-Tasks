package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.nikkap.calendar.core.utils.toDisplayDate
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import java.time.YearMonth


@Composable
fun GroupOfListItem(
    list: List<SplitEntity>,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onCompleteClick: (String, String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
        )
        {
            val currentMonth = remember { YearMonth.now() }
            val itemDate = remember { list[0].date.toLocalDate().year }
            Text(
                list[0].date.toLocalDate().toDisplayDate(
                    currentMonth.year != itemDate
                )
            )
        }
        list.filter { it !is SplitEntity.SubtaskItem }.forEach { entity ->
            when (entity) {
                is SplitEntity.TaskItem -> {
                    val parentId = entity.id
                    val listOfSubtasks = list.mapNotNull {
                        if (it is SplitEntity.SubtaskItem) {
                            if (it.subtask.parentId == parentId) it.subtask else null
                        } else null
                    }
                    ListTaskItem(
                        entity,
                        onEditClick = { id, type -> onEditClick(id, type) },
                        onCompleteClick = { id, type -> onCompleteClick(id, type) },
                        subtasks = listOfSubtasks,
                        onDeleteClick = { id, type -> onDeleteClick(id, type) }
                    )
                }

                is SplitEntity.EventItem -> ListEventItem(
                    item = entity,
                    onEditClick = { onEditClick(entity.id, "EVENT") },
                    onDeleteClick = { onDeleteClick(entity.id, "EVENT") }
                )

                is SplitEntity.BirthdayItem -> ListBirthdayItem(
                    entity,
                    onEditClick = { onEditClick(entity.id, "BIRTHDAY") },
                    onDeleteClick = { onDeleteClick(entity.id, "BIRTHDAY") }
                )

                else -> {}
            }
            HorizontalDivider()
        }
    }
}

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
    GroupOfListItem(
        list, { _, _ -> },
        onDeleteClick = { _, _ -> },
        onCompleteClick = { _, _ -> },
    )
}

