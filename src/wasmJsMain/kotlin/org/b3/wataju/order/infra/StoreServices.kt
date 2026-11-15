package org.b3.wataju.order.infra

import kotlinx.serialization.encodeToString
import org.b3.wataju.order.*
import org.b3.wataju.order.application.*
import org.b3.wataju.order.domain.reconcile

/** Web composition root. Both adapters use the same checkout workflow and stored keys. */
class StoreServices(config: StoreConfig, catalog: Catalog, language: String) {
    private val namespace = "wataju." + config.storeId + "." + (config.orderApi ?: "preview")
    val orders: OrderGateway = config.orderApi?.let { ServerOrders(it, language) }
        ?: PreviewOrders(catalog, namespace, ::readSession, ::writeSession, ::newKey)
    val checkout = OrderController(
        Checkout(orders, TabJournal(namespace), ::newKey),
        readSubmittedKey = { readSession("$namespace.submitted") },
        writeSubmittedKey = { writeSession("$namespace.submitted", it) },
    )
    val basket = StoredBasket("$namespace.basket", catalog)
}

class StoredBasket(private val key: String, private val catalog: Catalog) {
    fun load(): Map<String, Int> {
        val stored = runCatching { json.decodeFromString<Map<String, Int>>(preference(key)) }.getOrDefault(emptyMap())
        return reconcile(stored, catalog)
    }

    fun save(value: Map<String, Int>): Map<String, Int> = reconcile(value, catalog).also {
        savePreference(key, json.encodeToString(it))
    }
}
