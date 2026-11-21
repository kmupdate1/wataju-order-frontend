package org.b3.foundation.localization

import org.b3.foundation.application.Application
import org.b3.foundation.application.FeatureKey
import org.b3.foundation.application.Feature

object Localization : Feature<Application, LocalizationConfiguration, LocalizationService> {
    override val key: FeatureKey<LocalizationService> =
        FeatureKey("Localization")

    override fun configuration(): LocalizationConfiguration =
        LocalizationConfiguration()

    override fun adapt(application: Application, configuration: LocalizationConfiguration) =
        application.register(
            key = key,
            instance = LocalizationService(
                language = configuration.language,
            ),
        )
}
