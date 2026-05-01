package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import java.time.YearMonth

@Composable
fun List(
    itemsList: List<SplitEntity>,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String, String) -> Unit,
    onCompleteClick: (String, String) -> Unit,
) {
    val listState = rememberLazyListState()
    val currentMonth = remember { YearMonth.now() }
    val listOfListsWithSameDateItems: List<List<SplitEntity>> = itemsList
        .groupBy { it.date }
        .values
        .toList()
        .sortedBy { it[0].date.toLocalDate() }
////        .filter { it[0].date.toLocalDate().yearMonth > currentMonth.minusMonths(12) }
//    val mapOfParentToChildren: Map<String, List<String>> = itemsList
//        // 1. Оставляем только элементы типа SubtaskItem
//        .filterIsInstance<SplitEntity.SubtaskItem>()
//        // 2. Группируем. Ключ - родитель, Значения - список айдишников детей
//        .groupBy(
//            keySelector = { item -> item.subtask.parentId },
//            valueTransform = { item -> item.id }
//        )
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

//@Preview
//@Composable
//private fun Preview(){
//    List()
//}