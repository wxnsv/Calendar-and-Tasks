package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.R
import com.nikkap.calendar.core.utils.loremIpsum
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity

@Composable
fun ListTaskItem(
    item: SplitEntity.TaskItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit,
    subtasks: List<Subtask> = emptyList()
) {
    var isSubtasksVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(5.dp, end = 15.dp)
            .fillMaxWidth()
//            .wrapContentHeight()
            .clickable {
                isSubtasksVisible = !isSubtasksVisible
            }
    ) {
        Row {
            Text(
                item.title,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            DropDownMenu(
                completable = true,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onCompleteClick = onCompleteClick,
            )
        }
        Row {
            if (item.task.notes?.isBlank() == false) {
                Text(
                    item.task.notes,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .weight(3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.task),
                    contentDescription = "Task icon",

                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                "Task",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(60.dp),
//                    .padding(5.dp)
                color = Color.Black
            )
        }
        if (subtasks.isNotEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(
                        id = if (!isSubtasksVisible)
                            R.drawable.arrow_drop_down
                        else R.drawable.arrow_drop_up
                    ),
                    contentDescription = "Subtasks drop arrow icon",

                    modifier = Modifier.size(25.dp),
                )
            }
        }
    }
    AnimatedVisibility(visible = isSubtasksVisible) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 350.dp)
        ) {
            items(subtasks) { subtask ->
                ListSubtaskItem(
                    subtask,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onCompleteClick = onCompleteClick
                )
            }
        }
    }
}

@Preview
@Composable
private fun WithoutNotes() {
    val taskItem = SplitEntity.TaskItem(
        Task(
            id = "1",
            title = loremIpsum,
            deadline = 0L
        )
    )
    ListTaskItem(
        subtasks = emptyList(),
        item = taskItem,
        onEditClick = { },
        onDeleteClick = { },
        onCompleteClick = { }
    )
}

@Preview
@Composable
private fun WithDescription() {
    val taskItem = SplitEntity.TaskItem(
        Task(
            id = "1",
            title = loremIpsum,
            notes = loremIpsum,
            deadline = 0L
        )
    )
    ListTaskItem(
        subtasks = emptyList(),
        item = taskItem,
        onEditClick = { },
        onDeleteClick = { },
        onCompleteClick = { },
    )
}

@Preview
@Composable
private fun WithSubtasks() {
    val taskItem = SplitEntity.TaskItem(
        Task(
            id = "1",
            title = loremIpsum,
            deadline = 0L
        )
    )
    val subtasks = listOf(
        Subtask(
            id = "",
            title = loremIpsum,
            deadline = 0L,
            parentId = "1"
        ),
        Subtask(
            id = "",
            title = loremIpsum,
            deadline = 0L,
            parentId = "1"
        ), Subtask(
            id = "",
            title = loremIpsum,
            deadline = 0L,
            parentId = "1"
        )
    )
    ListTaskItemWithSubtasks(
        subtasks = subtasks,
        item = taskItem,
    )
}

@Composable
private fun ListTaskItemWithSubtasks(
    item: SplitEntity.TaskItem,
    subtasks: List<Subtask> = emptyList()
) {
    Column(
        modifier = Modifier
            .padding(5.dp, end = 15.dp)
            .fillMaxWidth()
//            .wrapContentHeight()
    ) {
        Row {
            Text(
                item.title,
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
//                DropDownMenu(
//                    onEditClick = { },
//                    onDeleteClick = { }
//                )
            }
        }
        Row {
            if (item.task.notes?.isBlank() == false) {
                Text(
                    item.task.notes,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.task),
                    contentDescription = "Task icon",

                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                "Task",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(60.dp),
//                    .padding(5.dp)
                color = Color.Black
            )
        }
        if (subtasks.isNotEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(
                        id = R.drawable.arrow_drop_down
                    ),
                    contentDescription = "Subtasks drop down icon",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        AnimatedVisibility(visible = true) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 350.dp)
            ) {
                items(subtasks) { subtask ->
                    ListSubtaskItem(
                        subtask, onEditClick = { },
                        onDeleteClick = { },
                        onCompleteClick = { }
                    )
                }
            }
        }
    }
}