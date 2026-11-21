package org.b3.foundation.ui.container

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class RowContainer(
    override val children: List<View>,
    private val modifier: Modifier = Modifier,
    private val horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    private val verticalAlignment: Alignment.Vertical = Alignment.Top,
) : Container() {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = { content() },
        )
    }
}
