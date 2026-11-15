package org.b3.dev.locale

/** Matches supported language tags while preserving the product's spelling for resource paths. */
class LocalePolicy(supported: List<String>) {
    private fun normalized(tag: String) = tag.trim().replace('_', '-').lowercase()
    private val tags = supported.associateBy(::normalized)

    init {
        require(tags.isNotEmpty() && "" !in tags && tags.size == supported.size)
    }

    fun match(tag: String?): String? {
        var key = tag?.let(::normalized) ?: return null
        while (key.isNotEmpty()) {
            tags[key]?.let { return it }
            key = key.substringBeforeLast('-', "")
        }
        return null
    }

    fun select(saved: String?, system: String?, fallback: String): String =
        match(saved) ?: match(system) ?: requireNotNull(match(fallback)) { "Unsupported default locale: $fallback" }
}

/** Blank translations are missing. The caller supplies its product-specific fallback order. */
fun Map<String, String>.translated(preferred: List<String>): String {
    val available = filterValues(String::isNotBlank)
    if (available.isEmpty()) return ""
    val byTag = available.entries.associate { it.key.replace('_', '-').lowercase() to it.value }
    for (language in preferred) {
        var key = language.replace('_', '-').lowercase()
        while (key.isNotEmpty()) {
            byTag[key]?.let { return it }
            key = key.substringBeforeLast('-', "")
        }
    }
    return available.values.first()
}
