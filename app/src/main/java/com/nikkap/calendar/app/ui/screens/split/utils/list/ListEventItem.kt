package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import com.nikkap.calendar.app.ui.utils.toColor
import com.nikkap.calendar.core.utils.loremIpsum
import com.nikkap.calendar.domain.model.Event

@Composable
fun ListEventItem(
    item: SplitEntity.EventItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Row {
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
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
            )
        }
        Row {
            if (item.event.description?.isBlank() == false) {
                Text(
                    item.event.description!!,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .weight(7f)
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .height(30.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.event),
                    contentDescription = "Event icon",
                    modifier = Modifier.size(25.dp),
                    colorFilter = ColorFilter.tint(
                        item.colorHex.toColor()
                    )
                )
            }

            Text(
                "Event",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .width(65.dp)
                    .height(30.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview
@Composable
private fun WithoutDescription() {
    CalendarTheme {
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
}

@Preview
@Composable
private fun WithDescription() {
    CalendarTheme {
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
}
