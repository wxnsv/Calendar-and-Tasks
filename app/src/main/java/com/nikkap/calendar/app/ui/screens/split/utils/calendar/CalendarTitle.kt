package com.nikkap.calendar.app.ui.screens.split.utils.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import java.time.YearMonth

@Composable
fun CalendarTitle(
    modifier: Modifier,
    currentMonth: YearMonth,
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = "${currentMonth.year} ${
                currentMonth.month.toString().lowercase().replaceFirstChar { it.uppercase() }
            }",
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
@Preview
private fun Preview() {
    CalendarTheme {
        CalendarTitle(
            modifier = Modifier,
            currentMonth = YearMonth.now()
        )
    }
}