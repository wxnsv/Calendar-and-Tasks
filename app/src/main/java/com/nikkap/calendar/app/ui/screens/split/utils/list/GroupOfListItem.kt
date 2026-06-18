package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupOfListItem(
    list: List<SplitEntity>,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onCompleteClick: (String, String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
                        onDeleteClick = { id, type -> onDeleteClick(id, type) },
                    )
                }

                is SplitEntity.EventItem -> ListEventItem(
                    item = entity,
                    onEditClick = { onEditClick(entity.id, "EVENT") },
                    onDeleteClick = { onDeleteClick(entity.id, "EVENT") },
                )

                is SplitEntity.BirthdayItem -> ListBirthdayItem(
                    entity,
                    onEditClick = { onEditClick(entity.id, "BIRTHDAY") },
                    onDeleteClick = { onDeleteClick(entity.id, "BIRTHDAY") },
                )

                else -> {}
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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

