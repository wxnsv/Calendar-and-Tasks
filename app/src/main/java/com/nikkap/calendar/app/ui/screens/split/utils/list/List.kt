package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import com.nikkap.calendar.core.utils.toLocalDate

@Composable
fun List(
    itemsList: List<SplitEntity>,
    itemsWithoutDateList: List<SplitEntity>,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    listState: LazyListState,
    onCompleteClick: (String, String) -> Unit,
) {
    val screenHeight = LocalWindowInfo.current.containerSize.height.div(8)
    val listOfListsWithSameDateItems: List<List<SplitEntity>> = itemsList
        .groupBy { it.date.toLocalDate() }
        .values
        .toList()
        .sortedBy { it[0].date.toLocalDate() }

    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = screenHeight.dp)) {
        listOfListsWithSameDateItems.forEach { list ->
            stickyHeader {
                GroupOfListItemTitle(list.first())
            }
            item {
                GroupOfListItem(
                    list, onEditClick,
                    onDeleteClick = onDeleteClick,
                    onCompleteClick = onCompleteClick,
                )
            }
        }
        if (itemsWithoutDateList.isNotEmpty()) {
            stickyHeader {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tasks without date",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
            items(itemsWithoutDateList) { task ->
                if (task is SplitEntity.TaskItem) {
                    val subtasks = itemsWithoutDateList.mapNotNull {
                        if (it is SplitEntity.SubtaskItem) {
                            if (it.subtask.parentId == task.id) it.subtask else null
                        } else null
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    ListTaskItem(
                        item = task,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onCompleteClick = onCompleteClick,
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "That end of list",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(top = 40.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

}