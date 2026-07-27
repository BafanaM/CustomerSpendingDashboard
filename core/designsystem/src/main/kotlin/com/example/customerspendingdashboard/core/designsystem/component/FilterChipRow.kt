package com.example.customerspendingdashboard.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme

/** A horizontally scrolling row of single-select filter chips over an arbitrary option type. */
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

// Preview of FilterChipRow over a plain string option list.
@Preview(showBackground = true)
@Composable
private fun FilterChipRowPreview() {
    SpendingTheme {
        FilterChipRow(
            options = listOf("Week", "Month", "Quarter", "Year"),
            selected = "Month",
            onSelect = {},
            label = { it },
        )
    }
}
