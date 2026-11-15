package org.b3.wataju.order.application

import org.b3.wataju.order.domain.Destination
import org.b3.wataju.order.domain.Order
import org.b3.wataju.order.domain.PendingOrder

class QuoteExpired : Exception()

sealed interface OrderState {
    data object Editing : OrderState
    data class Reviewing(val draft: PendingOrder) : OrderState
    data class AwaitingReceipt(val draft: PendingOrder) : OrderState
    data class Complete(val order: Order) : OrderState
}

/** Called on the UI dispatcher. Failure to receive a response preserves the retry identity. */
class OrderController(
    private val checkout: Checkout,
    readSubmittedKey: () -> String,
    private val writeSubmittedKey: (String) -> Unit,
) {
    var state: OrderState = checkout.restore()?.let { draft ->
        if (readSubmittedKey() == draft.key) OrderState.AwaitingReceipt(draft)
        else OrderState.Reviewing(draft)
    } ?: OrderState.Editing
        private set

    suspend fun review(cart: Map<String, Int>, destination: Destination) {
        check(state !is OrderState.AwaitingReceipt)
        state = OrderState.Reviewing(checkout.review(cart, destination))
    }

    fun edit() {
        check(state !is OrderState.AwaitingReceipt)
        discardDraft()
    }

    suspend fun confirm(): Order {
        val draft = when (val current = state) {
            is OrderState.Reviewing -> current.draft
            is OrderState.AwaitingReceipt -> current.draft
            else -> error("A reviewed quote is required")
        }
        writeSubmittedKey(draft.key)
        state = OrderState.AwaitingReceipt(draft)
        val order = try {
            checkout.confirm()
        } catch (expired: QuoteExpired) {
            discardDraft()
            throw expired
        }
        checkout.clear()
        writeSubmittedKey("")
        state = OrderState.Complete(order)
        return order
    }

    private fun discardDraft() {
        checkout.clear()
        writeSubmittedKey("")
        state = OrderState.Editing
    }
}
