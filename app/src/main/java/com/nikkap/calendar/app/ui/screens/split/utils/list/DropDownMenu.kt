package com.nikkap.calendar.app.ui.screens.split.utils.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikkap.calendar.app.R

@Composable
fun DropDownMenu(
    completable: Boolean = false,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxSize(),
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Icon(painterResource(R.drawable.more_vert), contentDescription = "Show item menu")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Edit",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                },
                onClick = {
                    expanded = false
                    onEditClick()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
            )
            if (completable) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Complete", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onCompleteClick()
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DropDownMenu(
        onEditClick = { },
        onDeleteClick = { }
    )
}