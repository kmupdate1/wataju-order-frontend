package org.b3.wataju.order

import org.b3.dev.locale.LocalePolicy
import org.b3.dev.locale.translated

internal val storeLanguages = LocalePolicy(listOf("ja", "en"))

fun Map<String, String>.localized(language: String, fallback: String): String =
    translated(listOf(language, fallback, "ja", "en"))
