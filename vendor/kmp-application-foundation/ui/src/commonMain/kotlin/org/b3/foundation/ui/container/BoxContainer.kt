package org.b3.foundation.ui.container

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class BoxContainer(
    override val children: List<View>,
    private val modifier: Modifier = Modifier,
    private val contentAlignment: Alignment = Alignment.TopStart,
) : Container() {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment,
            content = { content() },
        )
    }
}
