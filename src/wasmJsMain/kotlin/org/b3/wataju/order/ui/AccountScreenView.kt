package org.b3.wataju.order.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View
import org.b3.wataju.order.domain.Account
import org.b3.wataju.order.domain.Destination

class AccountScreenView(
    private val account: Account?, private val address: Destination,
    private val editing: Boolean, private val busy: Boolean,
    private val d: Display, private val theme: String,
    private val edit: (Destination) -> Unit, private val beginEdit: () -> Unit,
    private val save: () -> Unit, private val language: (String) -> Unit,
    private val appearance: (String) -> Unit,
) : View {
    @Composable override fun onRender() {
        account?.let {
            if (it.displayName.isNotBlank()) Text(it.displayName)
            if (editing) {
                if (!busy) AddressFormView(address, d, edit).onRender()
                Button(save, enabled = address.valid() && !busy) { Text(d.text("save")) }
            } else {
                if (it.destination.valid()) Text(listOf(it.destination.recipient, it.destination.postal, it.destination.address).joinToString(" · "))
                OutlinedButton(beginEdit, enabled = !busy) { Text(d.text("edit")) }
            }
        }
        Text(d.text("language"), style = MaterialTheme.typography.titleMedium)
        Row {
            listOf("ja" to "日本語", "en" to "English").forEach { (code, label) ->
                TextButton({ language(code) }, enabled = !busy && d.lang != code) { Text(label) }
            }
        }
        Text(d.text("theme"), style = MaterialTheme.typography.titleMedium)
        Row {
            listOf("system", "light", "dark").forEach { value ->
                TextButton({ appearance(value) }, enabled = theme != value) { Text(d.text(value)) }
            }
        }
    }
}
