package org.b3.wataju.order.infra

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.b3.wataju.order.Catalog
import org.b3.wataju.order.application.OrderGateway
import org.b3.wataju.order.domain.*

/** Tab-scoped example adapter. Receipts contain item snapshots. Profile and checkout state stay in this tab. */
class PreviewOrders(
    private val catalog: Catalog, private val namespace: String,
    private val readSession: (String) -> String, private val writeSession: (String, String) -> Unit,
    private val newKey: () -> String
) : OrderGateway {
    private val json = Json { ignoreUnknownKeys = true }
    override suspend fun account(): Account = readSession("$namespace.account").takeIf(String::isNotBlank)
        ?.let { json.decodeFromString(it) } ?: Account("")
    override suspend fun saveDestination(destination: Destination): Account {
        require(destination.valid())
        val profile = Account("", destination)
        writeSession("$namespace.account", json.encodeToString(profile))
        return profile
    }
    override suspend fun quote(request: QuoteRequest): Quote {
        require(request.destination.valid() && request.items.isNotEmpty())
        val lines = request.items.map { item ->
            require(item.quantity in 1..99)
            val p = catalog.products.single { it.id == item.sku }
            OrderLine(p.id, p.name, p.size, item.quantity, p.unitPrice)
        }
        val quote = Quote(newKey(), catalog.currency, lines, 0, 0, lines.sumOf { it.quantity * it.unitPrice })
        writeSession("$namespace.previewQuote", json.encodeToString(quote))
        return quote
    }
    override suspend fun place(quoteId: String, key: String): Order {
        val orders = history()
        orders.find { it.id == key }?.let { return it }
        val quote = json.decodeFromString<Quote>(readSession("$namespace.previewQuote"))
        require(quote.id == quoteId)
        val order = Order(key, quote)
        writeSession("$namespace.orders", json.encodeToString(listOf(order) + orders))
        return order
    }
    override suspend fun history(): List<Order> = readSession("$namespace.orders").takeIf { it.isNotBlank() }
        ?.let { json.decodeFromString(it) } ?: emptyList()
}
