package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nikkap.calendar.core.utils.toDisplayDate
import com.nikkap.calendar.core.utils.toLocalDate
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import java.time.YearMonth

@Composable
fun GroupOfListItemTitle(item: SplitEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Gray)
    )
    {
        val currentMonth = remember { YearMonth.now() }
        val itemDate = remember { item.date.toLocalDate().year }

        Text(
            item.date.toLocalDate().toDisplayDate(
                currentMonth.year != itemDate
            )
        )

    }
}