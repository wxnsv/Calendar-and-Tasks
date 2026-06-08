package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.core.utils.loremIpsum
import com.nikkap.calendar.domain.model.Subtask

@Composable
fun ListSubtaskItem(
    modifier: Modifier,
    subtask: Subtask,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 10.dp, horizontal = 5.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row {
            Text(
                subtask.title ?: "No title",
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
            DropDownMenu(
                completable = true,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onCompleteClick = onCompleteClick
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val subtask = Subtask(
        title = loremIpsum,
        deadline = 123543454312
    )
    ListSubtaskItem(
        Modifier, subtask, { },
        {}, {})
}