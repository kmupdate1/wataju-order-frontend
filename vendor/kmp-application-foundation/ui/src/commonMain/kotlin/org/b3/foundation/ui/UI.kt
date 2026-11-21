package org.b3.foundation.ui

import org.b3.foundation.application.Feature
import org.b3.foundation.application.FeatureKey
import org.b3.foundation.ui.view.View

object UI : Feature<UiApplication, UiConfiguration, View> {
    override val key: FeatureKey<View> =
        FeatureKey("UI")

    override fun configuration(): UiConfiguration =
        UiConfiguration()

    override fun adapt(application: UiApplication, configuration: UiConfiguration) =
        application.register(
            key = key,
            instance = configuration.createView(),
        )
}
