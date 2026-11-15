package org.b3.wataju.order.application

import org.b3.wataju.order.domain.*

/** A pending request survives reload and is retried with exactly the same key. */
interface CheckoutJournal {
    fun load(): PendingOrder?
    fun store(pending: PendingOrder)
    fun clear()
}

class Checkout(private val gateway: OrderGateway, private val journal: CheckoutJournal, private val newKey: () -> String) {
    fun restore() = journal.load()

    suspend fun review(cart: Map<String,Int>, destination: Destination): PendingOrder {
        require(cart.isNotEmpty() && cart.values.all { it in 1..99 } && destination.valid())
        val request = QuoteRequest(cart.map { BasketItem(it.key, it.value) }, destination)
        val quote = gateway.quote(request)
        require(quote.id.isNotBlank() && quote.currency.matches(Regex("[A-Z]{3}")) && quote.total >= 0)
        return PendingOrder(quote, destination, newKey()).also(journal::store)
    }

    suspend fun confirm(): Order {
        val pending = requireNotNull(restore())
        return gateway.place(pending.quote.id, pending.key)
    }

    fun clear() = journal.clear()
}
