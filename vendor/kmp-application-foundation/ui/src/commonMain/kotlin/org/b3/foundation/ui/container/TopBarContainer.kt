package org.b3.foundation.ui.container

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class TopBarContainer(
    override val children: List<View>,
    private val modifier: Modifier = Modifier,
) : Container() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        TopAppBar(
            modifier = modifier,
            title = { content() },
        )
    }
}
