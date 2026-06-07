package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity

@Composable
fun List(
    itemsList: List<SplitEntity>,
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
                    modifier = Modifier.padding(top = 40.dp),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

}