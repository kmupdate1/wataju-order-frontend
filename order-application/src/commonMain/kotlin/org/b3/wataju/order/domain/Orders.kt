package org.b3.wataju.order.domain

import kotlinx.serialization.Serializable
import org.b3.wataju.order.Catalog

@Serializable
data class Destination(val recipient: String = "", val postal: String = "", val address: String = "") {
    fun valid() = recipient.isNotBlank() && postal.isNotBlank() && address.isNotBlank()
}

@Serializable
data class BasketItem(val sku: String, val quantity: Int)

fun reconcile(quantities: Map<String, Int>, catalog: Catalog): Map<String, Int> =
    catalog.products.mapNotNull { p ->
        quantities[p.id]?.takeIf { it > 0 }?.let { p.id to it.coerceAtMost(99) }
    }.toMap()

@Serializable
data class OrderLine(val sku: String, val name: Map<String,String>, val size: String, val quantity: Int, val unitPrice: Long)

@Serializable
data class QuoteRequest(val items: List<BasketItem>, val destination: Destination)

@Serializable
data class Quote(val id: String, val currency: String, val lines: List<OrderLine>,
                 val shipping: Long, val tax: Long, val total: Long)

@Serializable
data class PendingOrder(val quote: Quote, val destination: Destination, val key: String)

@Serializable
data class Order(val id: String, val quote: Quote)

@Serializable
data class Account(val displayName: String, val destination: Destination = Destination())
