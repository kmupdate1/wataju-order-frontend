package org.b3.foundation.ui.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.b3.foundation.ui.view.View

class ScaffoldContainer(
    val topBar: View,
    val bottomBar: View,
    val body: View,
    private val modifier: Modifier = Modifier,
) : Container() {
    override val children: List<View> = listOf(topBar, bottomBar, body)

    @Composable
    override fun onRender() = Apply {
        body.onRender()
    }

    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        Scaffold(
            modifier = modifier,
            topBar = { topBar.onRender() },
            bottomBar = { bottomBar.onRender() },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues),
                    content = { content() },
                )
            },
        )
    }
}
