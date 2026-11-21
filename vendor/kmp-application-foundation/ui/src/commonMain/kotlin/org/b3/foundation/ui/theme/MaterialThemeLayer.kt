package org.b3.foundation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import org.b3.foundation.ui.view.ViewLayer

class MaterialThemeLayer(
    override val children: List<View>,
    private val configuration: ThemeConfiguration,
) : ViewLayer {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) =
        MaterialTheme(
            colorScheme = configuration.colorScheme(),
            shapes = configuration.shapes,
            typography = configuration.typography,
            content = content,
        )
}
