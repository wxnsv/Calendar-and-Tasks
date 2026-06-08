package com.nikkap.calendar.app.ui.screens.split.utils.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import java.time.LocalDate

@Composable
fun Day(
    day: CalendarDay,
    onClick: () -> Unit,
    isSelected: Boolean,
    colorsList: List<Color> = emptyList()
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(top = 1.dp, start = 1.dp)
            .clickable(
                enabled = day.position == DayPosition.MonthDate && colorsList.isNotEmpty(),
                onClick = { onClick() }
            )
            .background(color = MaterialTheme.colorScheme.background)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            )
    ) {
        val dayPositionColor = when (day.position) {
            DayPosition.InDate -> MaterialTheme.colorScheme.error
            DayPosition.MonthDate -> MaterialTheme.colorScheme.onBackground
            DayPosition.OutDate -> MaterialTheme.colorScheme.secondary
        }

        Text(
            text = day.date.dayOfMonth.toString(),
            color = dayPositionColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 4.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(27.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (color in colorsList) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(color.copy(alpha = 0.5f))
//                        .border(0.3F.dp, color = Color.Black),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    CalendarTheme {
        val day = CalendarDay(
            LocalDate.of(2026, 4, 20),
            position = DayPosition.MonthDate,
        )
        Day(
            day = day,
            onClick = {},
            isSelected = false
        )
        Day(
            day = day,
            onClick = {},
            isSelected = true
        )
        Day(
            day = day,
            onClick = {},
            isSelected = false,
            colorsList = listOf(Color.DarkGray, Color.Gray, Color.Magenta)
        )
    }
}