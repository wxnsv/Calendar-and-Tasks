package com.nikkap.calendar.ui.screens.split.utils.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.nikkap.calendar.R

@Composable
fun DropDownMenu(
    completable: Boolean = false,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.Center)) {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.more_vert), contentDescription = "Show item menu")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEditClick()
                },
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
            )
            if (completable) {
                DropdownMenuItem(
                    text = { Text("Complete") },
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