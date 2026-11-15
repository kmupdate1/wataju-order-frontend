package org.b3.wataju.order

import kotlin.coroutines.*
import kotlin.test.*
import org.b3.wataju.order.application.*
import org.b3.wataju.order.domain.*
import org.b3.wataju.order.infra.PreviewOrders

class CheckoutTest {
    private val address = Destination("A", "100", "Street")
    private val product = Product("s", mapOf("ja" to "醤油"), emptyMap(), "500 mL", 700)
    private val catalog = Catalog("JPY", listOf(product))

    @Test fun persistedBasketUsesCurrentSkuAndQuantityLimits() {
        assertEquals(mapOf("s" to 99), reconcile(mapOf("s" to 120, "gone" to 2), catalog))
        assertTrue(reconcile(mapOf("s" to 0), catalog).isEmpty())
    }

    @Test fun missingTranslationUsesConfiguredFallback() {
        assertEquals("醤油", product.name.localized("en", "ja"))
    }

    @Test fun retryAfterLostResponseUsesOriginalQuoteAndKey() = immediate {
        val journal = MemoryJournal()
        val gateway = TestGateway()
        val checkout = Checkout(gateway, journal) { "same-key" }
        val draft = checkout.review(mapOf("s" to 2), address)
        assertEquals(1700L, draft.quote.total)
        assertFails { checkout.confirm() }
        val resumed = Checkout(gateway, journal) { error("must reuse existing key") }
        assertEquals("receipt", resumed.confirm().id)
        assertEquals(listOf("q:same-key", "q:same-key"), gateway.attempts)
        resumed.clear()
        assertNull(resumed.restore())
    }

    @Test fun invalidInputNeverRequestsQuote() = immediate {
        val gateway = TestGateway()
        val checkout = Checkout(gateway, MemoryJournal()) { "key" }
        assertFails { checkout.review(emptyMap(), address) }
        assertFails { checkout.review(mapOf("s" to 100), address) }
        assertFails { checkout.review(mapOf("s" to 1), Destination()) }
        assertEquals(0, gateway.quotes)
    }

    @Test fun demoReceiptSurvivesReloadAndCatalogChanges() = immediate {
        val store = mutableMapOf<String, String>()
        val open = { current: Catalog ->
            PreviewOrders(current, "test", { store[it].orEmpty() }, { k, v -> store[k] = v }, { "q" })
        }
        val demo = open(catalog)
        demo.saveDestination(address)
        val quote = demo.quote(QuoteRequest(listOf(BasketItem("s", 2)), address))
        val receipt = demo.place(quote.id, "order-1")
        val changed = open(catalog.copy(products = listOf(product.copy(unitPrice = 999))))
        assertEquals(receipt, changed.place(quote.id, "order-1"))
        assertEquals(1400L, changed.history().single().quote.total)
        assertEquals(address, changed.account().destination)
    }

    private class MemoryJournal : CheckoutJournal {
        private var value: PendingOrder? = null
        override fun load() = value
        override fun store(pending: PendingOrder) { value = pending }
        override fun clear() { value = null }
    }

    private class TestGateway : OrderGateway {
        val attempts = mutableListOf<String>()
        var quotes = 0
        val snapshot = Quote("q", "JPY", listOf(OrderLine("s", mapOf("ja" to "醤油"), "500 mL", 2, 700)), 200, 100, 1700)
        override suspend fun quote(request: QuoteRequest): Quote { quotes++; return snapshot }
        override suspend fun place(quoteId: String, key: String): Order {
            attempts += "$quoteId:$key"
            if (attempts.size == 1) error("Response lost")
            return Order("receipt", snapshot)
        }
        override suspend fun history() = emptyList<Order>()
        override suspend fun saveDestination(destination: Destination) = Account("", destination)
        override suspend fun account() = Account("")
    }
    // These in-memory ports complete synchronously.
    private fun immediate(block: suspend () -> Unit) {
        var result: Result<Unit>? = null
        block.startCoroutine(object : Continuation<Unit> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(value: Result<Unit>) { result = value }
        })
        requireNotNull(result).getOrThrow()
    }
}
