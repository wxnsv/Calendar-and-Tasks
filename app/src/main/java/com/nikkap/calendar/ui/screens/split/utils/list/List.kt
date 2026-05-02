package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity

@Composable
fun List(
    itemsList: List<SplitEntity>,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onCompleteClick: (String, String) -> Unit,
) {
    val listState = rememberLazyListState()
    val listOfListsWithSameDateItems: List<List<SplitEntity>> = itemsList
        .groupBy { it.date }
        .values
        .toList()
        .sortedBy { it[0].date.toLocalDate() }
    LazyColumn(state = listState) {
        items(listOfListsWithSameDateItems) {
            GroupOfListItem(
                it, onEditClick,
                onDeleteClick = onDeleteClick,
                onCompleteClick = onCompleteClick
            )
        }
    }
}