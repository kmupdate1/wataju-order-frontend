package org.b3.wataju.order

import kotlinx.serialization.Serializable

@Serializable
data class StoreConfig(
    val catalogUrl: String,
    val storeName: Map<String, String>,
    val defaultLocale: String = "ja"
)

@Serializable
data class Catalog(val currency: String, val products: List<Product>) {
    fun validated(): Catalog = apply {
        require(currency.matches(Regex("[A-Z]{3}")))
        require(products.map { it.id }.distinct().size == products.size)
        require(products.all { it.id.isNotBlank() && it.name.values.any(String::isNotBlank) && it.unitPrice in 0..1_000_000_000 })
    }
}

// One row per orderable SKU. Prices are whole major currency units in v0.1.
@Serializable
data class Product(
    val id: String,
    val name: Map<String, String>,
    val description: Map<String, String>,
    val size: String,
    val unitPrice: Long
)

fun Map<String, String>.localized(language: String, fallback: String): String =
    listOf(language, fallback, "ja", "en").firstNotNullOfOrNull { get(it)?.takeIf(String::isNotBlank) }
        ?: values.firstOrNull(String::isNotBlank).orEmpty()
