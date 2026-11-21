package org.b3.foundation.ui.surface

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import org.b3.foundation.ui.view.ViewLayer

class SurfaceLayer(
    override val children: List<View>,
    private val configuration: SurfaceConfiguration,
) : ViewLayer {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) {
        val color = configuration.color
            ?: MaterialTheme.colorScheme.background

        Surface(
            modifier = configuration.modifier,
            color = color,
            contentColor = configuration.contentColor
                ?: contentColorFor(color),
            content = content,
        )
    }
}
