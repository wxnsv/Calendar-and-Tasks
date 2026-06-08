package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nikkap.calendar.app.ui.screens.split.utils.SplitEntity
import com.nikkap.calendar.core.utils.toDisplayDate
import com.nikkap.calendar.core.utils.toLocalDate
import java.time.YearMonth

@Composable
fun GroupOfListItemTitle(item: SplitEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
    )
    {
        val currentMonth = remember { YearMonth.now() }
        val itemDate = remember { item.date.toLocalDate().year }
        Text(
            item.date.toLocalDate().toDisplayDate(
                currentMonth.year != itemDate
            ), modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}