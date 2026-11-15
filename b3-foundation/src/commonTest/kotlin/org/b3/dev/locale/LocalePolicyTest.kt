package org.b3.dev.locale

import kotlin.test.*

class LocalePolicyTest {
    @Test fun savedSystemDefaultOrder() {
        val policy = LocalePolicy(listOf("ja", "en"))
        assertEquals("en", policy.select("EN_gb", "ja", "ja"))
        assertEquals("ja", policy.select("fr", "JA_JP", "en"))
        assertEquals("en", policy.select(null, "ko", "en"))
    }

    @Test fun scriptAndRegionMatchBeforeBaseLanguage() {
        val policy = LocalePolicy(listOf("zh", "zh-Hant", "zh-CN"))
        assertEquals("zh-Hant", policy.match("zh_Hant_TW"))
        assertEquals("zh-CN", policy.match("zh-cn"))
        assertEquals("zh", policy.match("zh-SG"))
    }

    @Test fun blankTranslationFallsThrough() {
        assertEquals("醤油", mapOf("en" to " ", "ja" to "醤油").translated(listOf("en-US", "ja")))
        assertEquals("", emptyMap<String, String>().translated(listOf("ja")))
    }
}
