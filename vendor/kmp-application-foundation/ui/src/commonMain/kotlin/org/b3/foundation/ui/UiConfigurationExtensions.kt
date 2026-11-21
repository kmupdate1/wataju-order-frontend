package org.b3.foundation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.b3.foundation.annotation.B3Dsl
import org.b3.foundation.localization.Localization
import org.b3.foundation.localization.LocalizationState
import org.b3.foundation.ui.container.BoxContainer
import org.b3.foundation.ui.localization.LocalizationLayer
import org.b3.foundation.ui.surface.SurfaceConfiguration
import org.b3.foundation.ui.surface.SurfaceLayer
import org.b3.foundation.ui.theme.MaterialThemeLayer
import org.b3.foundation.ui.theme.ThemeConfiguration
import org.b3.foundation.ui.view.View
import org.b3.foundation.ui.view.ViewHierarchy

fun UiConfiguration.localization(language: LocalizationState? = null) =
    layer(ViewHierarchy.LOCALIZATION) { child ->
        val service = UiApplication.getOrNull(Localization.key)

        LocalizationLayer(
            children = listOf(child),
            localization = language
                ?: service?.language
                ?: LocalizationState.Default,
        )
    }

fun UiConfiguration.theme(
    configure: @B3Dsl ThemeConfiguration.() -> Unit = {},
) {
    val configuration = ThemeConfiguration()
        .apply(configure)

    layer(ViewHierarchy.THEME) { child ->
        MaterialThemeLayer(
            children = listOf(child),
            configuration = configuration,
        )
    }
}

fun UiConfiguration.surface(
    configure: @B3Dsl SurfaceConfiguration.() -> Unit = {},
) {
    val configuration = SurfaceConfiguration()
        .apply(configure)

    layer(ViewHierarchy.SURFACE) { child ->
        val content = if (configuration.isCentered)
            BoxContainer(
                children = listOf(child),
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            )
        else child

        SurfaceLayer(
            children = listOf(content),
            configuration = configuration,
        )
    }
}

fun UiConfiguration.overlayHost(factory: (View) -> View) =
    layer(
        hierarchy = ViewHierarchy.OVERLAY_HOST,
        factory = factory,
    )
