package org.b3.foundation.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.b3.foundation.localization.LocalizationState
import org.b3.foundation.ui.view.View
import org.b3.foundation.ui.view.ViewLayer

class LocalizationLayer(
    override val children: List<View>,
    private val localization: LocalizationState,
) : ViewLayer {
    @Composable
    override fun Apply(content: @Composable (() -> Unit)) =
        CompositionLocalProvider(
            value = LocalLocalization provides localization,
            content = content,
        )
}
