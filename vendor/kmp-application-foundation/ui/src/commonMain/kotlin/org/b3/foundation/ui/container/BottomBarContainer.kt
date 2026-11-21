package org.b3.foundation.ui.container

import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class BottomBarContainer(
    override val children: List<View>,
    private val modifier: Modifier = Modifier,
) : Container() {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        BottomAppBar(
            modifier = modifier,
            content = { content() },
        )
    }
}
