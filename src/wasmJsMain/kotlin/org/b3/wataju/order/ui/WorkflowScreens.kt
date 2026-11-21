package org.b3.wataju.order.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import org.b3.wataju.order.*
import org.b3.wataju.order.domain.*
import org.b3.wataju.order.application.*

class HomeScreenView(
    private val config: StoreConfig, private val d: Display, private val basketCount: Int,
    private val browse: () -> Unit, private val cart: () -> Unit,
) : View {
    @Composable override fun onRender() {
        config.notices.forEach { Text(d.name(it)) }
        Button(browse) { Text(d.text("browse")) }
        if (basketCount > 0) OutlinedButton(cart) { Text(d.text("cart") + " ($basketCount)") }
    }
}

class ProductDetailScreenView(
    private val product: Product, private val currency: String, private val d: Display,
    private val canAdd: Boolean, private val add: () -> Unit,
    private val cart: () -> Unit, private val back: () -> Unit,
) : View {
    @Composable override fun onRender() {
        ProductInfoView(product, currency, d).onRender()
        Text("SKU " + product.id)
        AddToCartButtonView(d.text("add"), canAdd, add).onRender()
        OutlinedButton(cart) { Text(d.text("cart")) }
        TextButton(back) { Text(d.text("catalog")) }
    }
}

class DeliveryScreenView(
    private val address: Destination, private val d: Display, private val busy: Boolean,
    private val hasItems: Boolean, private val edit: (Destination) -> Unit,
    private val review: () -> Unit,
) : View {
    @Composable override fun onRender() {
        if (!busy) AddressFormView(address, d, edit).onRender()
        Button(review, enabled = !busy && address.valid() && hasItems) { Text(d.text("review")) }
    }
}

class ReviewScreenView(
    private val draft: PendingOrder, private val d: Display,
    private val busy: Boolean, private val submitted: Boolean,
    private val confirm: () -> Unit, private val edit: () -> Unit,
) : View {
    @Composable override fun onRender() {
        Text(listOf(draft.destination.recipient, draft.destination.postal, draft.destination.address).joinToString(" · "))
        SummaryView(draft.quote, d).onRender()
        ConfirmOrderButtonView(d.text(if (submitted) "retryOrder" else "placeOrder"), !busy, confirm).onRender()
        if (!submitted) TextButton(edit, enabled = !busy) { Text(d.text("edit")) }
    }
}

class ConfirmOrderButtonView(label: String, enabled: Boolean, confirm: () -> Unit) :
    org.b3.dev.ui.ExecuteButtonView(label, enabled, confirm)

class CompleteScreenView(private val order: Order, private val d: Display, private val history: () -> Unit) : View {
    @Composable override fun onRender() {
        Text(d.text("orderNumber") + " " + order.id)
        SummaryView(order.quote, d).onRender()
        Button(history) { Text(d.text("history")) }
    }
}
