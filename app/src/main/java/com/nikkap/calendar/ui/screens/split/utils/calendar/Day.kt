package com.nikkap.calendar.ui.screens.split.utils.calendar

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
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
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
            )
            .padding(top = 1.dp, start = 1.dp)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick() }
            )
            .background(color = Color.Gray)
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Red,
            modifier = Modifier.align(Alignment.TopEnd)
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
                        .background(color),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
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