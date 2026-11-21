package org.b3.foundation.ui.container

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class ColumnContainer(
    override val children: List<View>,
    private val modifier: Modifier = Modifier,
    private val verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    private val horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) : Container() {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = { content() },
        )
    }
}
