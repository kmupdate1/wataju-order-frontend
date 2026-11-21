package org.b3.dev.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class AdaptiveGridView<T>(private val items: List<T>, private val columnsFor: (Dp) -> Int, private val gap: Dp = 12.dp,
                     private val cell: @Composable (T, Modifier) -> Unit) : View {
    @Composable
    override fun onRender() {
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

}
