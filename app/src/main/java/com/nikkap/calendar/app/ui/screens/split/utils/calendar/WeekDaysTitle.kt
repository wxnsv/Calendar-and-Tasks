package com.nikkap.calendar.app.ui.screens.split.utils.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.daysOfWeek
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import java.time.DayOfWeek
import java.time.format.TextStyle

@Composable
fun WeekDaysTitle(daysOfWeek: List<DayOfWeek>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )
        Row {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = dayOfWeek.getDisplayName(
                        TextStyle.SHORT,
                        java.util.Locale.getDefault()
                    ),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        WeekDaysTitle(daysOfWeek)
    }
}