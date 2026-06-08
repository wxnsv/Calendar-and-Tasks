package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.nikkap.calendar.domain.model.Birthday

@Composable
fun ListBirthdayItem(
    item: SplitEntity.BirthdayItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        Row {
            Text(
                item.title,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(6f)
                    .padding(horizontal = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
            DropDownMenu(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
        Row {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Image(
                    painter = painterResource(id = R.drawable.birthday),
                    contentDescription = "Birthday icon",
                    modifier = Modifier.size(25.dp),
                    colorFilter = ColorFilter.tint(
                        color = item.colorHex.toColor()
                    )
                )
            }

            Text(
                "Birthday",
                fontSize = 25.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(100.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val taskItem = SplitEntity.BirthdayItem(
        Birthday(
            id = "1",
            name = loremIpsum,
            date = 123123123120L
        )
    )
    CalendarTheme {
        ListBirthdayItem(
            item = taskItem,
            onEditClick = { },
            onDeleteClick = { },
        )
    }
}