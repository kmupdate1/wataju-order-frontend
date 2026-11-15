package org.b3.wataju.order

import kotlinx.serialization.Serializable

@Serializable
data class StoreConfig(
    val catalogUrl: String,
    val storeName: Map<String, String>,
    val defaultLocale: String = "ja",
    val orderApi: String? = null,
    val loginUrl: String? = null,
    val storeId: String = "wataju",
    val notices: List<Map<String, String>> = emptyList()
)
