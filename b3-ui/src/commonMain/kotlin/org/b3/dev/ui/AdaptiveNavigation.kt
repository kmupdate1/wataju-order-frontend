package org.b3.dev.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Layout owns placement; the product owns destinations, labels, icons and transition rules. */
@Composable
fun <T> AdaptiveNavigation(
    destinations: List<T>,
    selected: T,
    enabled: Boolean,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    icon: @Composable (T) -> Unit,
    header: @Composable () -> Unit,
    compactBelow: Dp = 720.dp,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < compactBelow
        Column {
            header()
            if (!compact) Row {
                destinations.forEach { item ->
                    TextButton({ onSelect(item) }, enabled = enabled) { Text(label(item)) }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) { content() }
            if (compact) NavigationBar {
                destinations.forEach { item ->
                    NavigationBarItem(selected = item == selected, onClick = { onSelect(item) },
                        enabled = enabled, icon = { icon(item) },
                        label = { Text(label(item), maxLines = 1) })
                }
            }
        }
    }
}
