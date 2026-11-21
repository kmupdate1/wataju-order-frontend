package org.b3.wataju.order.ui

import org.b3.foundation.ui.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import org.b3.wataju.order.Catalog
import org.b3.wataju.order.domain.*

class BasketView(private val catalog: Catalog, private val quantities: Map<String,Int>, private val d: Display, private val change: (String, Int) -> Unit, private val next: () -> Unit) : View {
    @Composable
    override fun onRender() {
        val lines = catalog.products.filter { it.id in quantities }
        if (lines.isEmpty()) { Text(d.text("empty")); return }
        lines.forEach { p ->
            val qty = quantities.getValue(p.id)
            Text(d.name(p.name) + " · " + p.size, style = MaterialTheme.typography.titleMedium)
            Text(d.money(p.unitPrice, catalog.currency) + " × " + qty + " = " + d.money(p.unitPrice * qty, catalog.currency))
            Row {
                TextButton({ change(p.id, qty - 1) }, enabled = qty > 1) { Text(d.text("decrease")) }
                TextButton({ change(p.id, qty + 1) }, enabled = qty < 99) { Text(d.text("increase")) }
                TextButton({ change(p.id, 0) }) { Text(d.text("remove")) }
            }
            HorizontalDivider()
        }
        Text(d.text("subtotal") + "  " + d.money(lines.sumOf { it.unitPrice * quantities.getValue(it.id) }, catalog.currency))
        Text(d.text("estimate"))
        Button(next) { Text(d.text("delivery")) }
    }
}

class AddressFormView(private val address: Destination, private val d: Display, private val edit: (Destination) -> Unit) : View {
    @Composable
    override fun onRender() {
        OutlinedTextField(address.recipient, { edit(address.copy(recipient = it)) }, singleLine = true, label = { Text(d.text("recipient")) })
        OutlinedTextField(address.postal, { edit(address.copy(postal = it)) }, singleLine = true, label = { Text(d.text("postal")) })
        OutlinedTextField(address.address, { edit(address.copy(address = it)) }, label = { Text(d.text("address")) })
    }
}

class SummaryView(private val quote: Quote, private val d: Display) : View {
    @Composable
    override fun onRender() {
        quote.lines.forEach { line ->
            Text(d.name(line.name) + " · " + line.size + " × " + line.quantity + "   " +
                d.money(line.unitPrice * line.quantity, quote.currency))
        }
        HorizontalDivider()
        listOf("shipping" to quote.shipping, "tax" to quote.tax, "total" to quote.total).forEach { (label, value) ->
            Text(d.text(label) + "  " + d.money(value, quote.currency))
        }
    }
}

class OrderHistoryView(private val orders: List<Order>, private val d: Display, private val reorder: (Order) -> Unit) : View {
    @Composable
    override fun onRender() {
        if (orders.isEmpty()) Text(d.text("emptyHistory"))
        orders.forEach { order ->
            Text(d.text("orderNumber") + " " + order.id, style = MaterialTheme.typography.titleMedium)
            SummaryView(order.quote, d).onRender()
            OutlinedButton({ reorder(order) }) { Text(d.text("reorder")) }
            Spacer(androidx.compose.ui.Modifier.height(8.dp))
        }
    }
}
