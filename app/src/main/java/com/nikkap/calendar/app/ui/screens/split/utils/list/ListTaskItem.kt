package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.app.R
import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import com.nikkap.calendar.app.ui.utils.toColor
import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.core.utils.loremIpsum
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task

@Composable
fun ListTaskItem(
    item: SplitEntity.TaskItem,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onCompleteClick: (String, String) -> Unit,
    subtasks: List<Subtask> = emptyList()
) {
    var isSubtasksVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .clickable {
                isSubtasksVisible = !isSubtasksVisible
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp)
        ) {
            Text(
                item.title,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
            DropDownMenu(
                completable = true,
                onEditClick = { onEditClick(item.id, "TASK") },
                onDeleteClick = { onDeleteClick(item.id, "TASK") },
                onCompleteClick = { onCompleteClick(item.id, "TASK") },
            )
        }
        Row {
            if (item.task.notes?.isBlank() == false) {
                Text(
                    item.task.notes!!,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .weight(3f)
                        .padding(horizontal = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
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
                    modifier = Modifier.size(25.dp),
                    colorFilter = ColorFilter.tint(
                        color = CalendarColors.getTaskColor().toColor()
                    )
                )
            }
            Text(
                "Task",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(60.dp)
                    .padding(end = 5.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
        if (subtasks.isNotEmpty()) {
            val rotationAngle by animateFloatAsState(
                targetValue = if (isSubtasksVisible) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "RotationAnimation"
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(R.drawable.arrow_drop_down),
                    contentDescription = "Subtasks drop down icon",
                    modifier = Modifier
                        .size(20.dp)
                        .scale(2f)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = isSubtasksVisible) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    items(
                        subtasks,
                        key = { subtask -> subtask.id }) { subtask ->
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ListSubtaskItem(
                            Modifier.animateItem(
                                fadeInSpec = tween(durationMillis = 300),
                                fadeOutSpec = tween(durationMillis = 300),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            subtask,
                            onEditClick = { onEditClick(subtask.id, "SUBTASK") },
                            onDeleteClick = { onDeleteClick(subtask.id, "SUBTASK") },
                            onCompleteClick = { onCompleteClick(subtask.id, "SUBTASK") },
                        )
                    }
                }
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
        onEditClick = {
        } as (String, String) -> Unit,
        onDeleteClick = { } as (String, String) -> Unit,
        onCompleteClick = { } as (String, String) -> Unit,
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
        onEditClick = { } as (String, String) -> Unit,
        onDeleteClick = { } as (String, String) -> Unit,
        onCompleteClick = { } as (String, String) -> Unit,
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
            id = "2",
            title = loremIpsum,
            deadline = 0L,
            parentId = "1"
        ),
        Subtask(
            id = "3",
            title = loremIpsum,
            deadline = 0L,
            parentId = "1"
        ), Subtask(
            id = "4",
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
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Row {
            Text(
                item.title,
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                DropDownMenu(
                    onEditClick = { },
                    onDeleteClick = { }
                )
            }
        }
        Row {
            if (item.task.notes?.isBlank() == false) {
                Text(
                    item.task.notes!!,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .weight(3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (subtasks.isNotEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_drop_down),
                    contentDescription = "Subtasks drop down icon",
                    modifier = Modifier
                        .size(20.dp)
                        .scale(3f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AnimatedVisibility(visible = true) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 350.dp),
            ) {
                items(
                    subtasks,
                    key = { subtask -> subtask.id }
                ) { subtask ->
                    ListSubtaskItem(
                        Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 300),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        subtask, onEditClick = { },
                        onDeleteClick = { },
                        onCompleteClick = { }
                    )
                }
            }
        }
    }
}