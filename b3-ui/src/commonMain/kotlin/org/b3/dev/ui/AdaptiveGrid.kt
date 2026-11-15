package org.b3.dev.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> AdaptiveGrid(items: List<T>, columnsFor: (Dp) -> Int, gap: Dp = 12.dp,
                     cell: @Composable (T, Modifier) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = columnsFor(maxWidth)
        require(columns > 0)
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            items.chunked(columns).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    group.forEach { item -> cell(item, Modifier.weight(1f)) }
                    repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
