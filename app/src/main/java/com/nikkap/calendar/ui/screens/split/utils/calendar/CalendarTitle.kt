package com.nikkap.calendar.ui.screens.split.utils.calendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

@Composable
fun CalendarTitle(
    modifier: Modifier,
    currentMonth: YearMonth,
) {
    Row(
        modifier = modifier.height(25.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = "${currentMonth.year} ${
                currentMonth.month.toString().lowercase().replaceFirstChar { it.uppercase() }
            }",
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}