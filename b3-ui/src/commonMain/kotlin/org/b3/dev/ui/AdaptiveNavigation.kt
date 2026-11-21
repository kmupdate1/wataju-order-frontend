package org.b3.dev.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Layout owns placement; the product owns destinations, labels, icons and transition rules. */
class AdaptiveNavigationView<T>(
    private val destinations: List<T>,
    private val selected: T,
    private val enabled: Boolean,
    private val onSelect: (T) -> Unit,
    private val label: (T) -> String,
    private val icon: @Composable (T) -> Unit,
    private val header: @Composable () -> Unit,
    private val compactBelow: Dp = 720.dp,
    private val content: @Composable () -> Unit,
) : View {
    @Composable
    override fun onRender() {
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

}
