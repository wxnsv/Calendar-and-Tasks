package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity

@Composable
fun ListEventItem(
    item: SplitEntity.EventItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            DropDownMenu(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
            )
        }
        Row {
            if (item.event.description?.isBlank() == false) {
                Text(
                    item.event.description,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .weight(6f)
                        .fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.DarkGray
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.event),
                    contentDescription = "Event icon",

                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                "Event",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(65.dp)
                    .height(30.dp),
//                    .padding(5.dp)
                color = Color.Black
            )
        }
    }
}

@Preview
@Composable
private fun WithoutDescription() {
    val eventItem = SplitEntity.EventItem(
        Event(
            id = "1",
            summary = loremIpsum,
            startTimestamp = 123123123120L
        )
    )
    ListEventItem(
        item = eventItem,
        onEditClick = { },
        onDeleteClick = { },
    )
}

@Preview
@Composable
private fun WithDescription() {
    val eventItem = SplitEntity.EventItem(
        Event(
            id = "1",
            summary = loremIpsum,
            description = loremIpsum,
            startTimestamp = 123123123120L
        )
    )
    ListEventItem(
        item = eventItem,
        onEditClick = { },
        onDeleteClick = { },
    )
}
