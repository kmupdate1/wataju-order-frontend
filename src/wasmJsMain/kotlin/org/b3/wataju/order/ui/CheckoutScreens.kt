package org.b3.wataju.order.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import org.b3.wataju.order.Catalog
import org.b3.wataju.order.domain.*

@Composable
fun Basket(catalog: Catalog, quantities: Map<String,Int>, d: Display, change: (String, Int) -> Unit, next: () -> Unit) {
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

@Composable
fun AddressForm(address: Destination, d: Display, edit: (Destination) -> Unit) {
    OutlinedTextField(address.recipient, { edit(address.copy(recipient = it)) }, singleLine = true, label = { Text(d.text("recipient")) })
    OutlinedTextField(address.postal, { edit(address.copy(postal = it)) }, singleLine = true, label = { Text(d.text("postal")) })
    OutlinedTextField(address.address, { edit(address.copy(address = it)) }, label = { Text(d.text("address")) })
}

@Composable
fun Summary(quote: Quote, d: Display) {
    quote.lines.forEach { line ->
        Text(d.name(line.name) + " · " + line.size + " × " + line.quantity + "   " +
            d.money(line.unitPrice * line.quantity, quote.currency))
    }
    HorizontalDivider()
    listOf("shipping" to quote.shipping, "tax" to quote.tax, "total" to quote.total).forEach { (label, value) ->
        Text(d.text(label) + "  " + d.money(value, quote.currency))
    }
}

@Composable
fun OrderHistory(orders: List<Order>, d: Display, reorder: (Order) -> Unit) {
    if (orders.isEmpty()) Text(d.text("emptyHistory"))
    orders.forEach { order ->
        Text(d.text("orderNumber") + " " + order.id, style = MaterialTheme.typography.titleMedium)
        Summary(order.quote, d)
        OutlinedButton({ reorder(order) }) { Text(d.text("reorder")) }
        Spacer(androidx.compose.ui.Modifier.height(8.dp))
    }
}
