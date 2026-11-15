package org.b3.wataju.order.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.ktor.client.request.get
import io.ktor.client.call.body
import kotlinx.coroutines.CancellationException
import org.b3.wataju.order.*

class Display(val text: (String) -> String, val lang: String, val fallback: String) {
    fun name(value: Map<String,String>) = value.localized(lang, fallback)
    fun money(value: Long, currency: String) = formatMoney(value.toDouble(), currency, lang)
}

@Composable
private fun ProductImage(product: Product, d: Display) {
    val url = product.imageUrl ?: return
    val bitmap by produceState<ImageBitmap?>(null, url) {
        value = null
        try {
            val bytes: ByteArray = http.get(resolveUrl(url)).body()
            value = org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { value = null }
    }
    bitmap?.let { Image(it, d.name(product.name), Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Fit) }
}

@Composable
fun ProductInfo(p: Product, currency: String, d: Display) {
    ProductImage(p, d)
    Text(d.name(p.name), style = MaterialTheme.typography.titleLarge)
    Text(d.name(p.description))
    Text(p.size + " · " + d.money(p.unitPrice, currency))
}

@Composable
fun Catalogue(catalog: Catalog, d: Display, query: String, search: (String) -> Unit, details: (Product) -> Unit) {
    OutlinedTextField(query, search, label = { Text(d.text("search")) }, singleLine = true)
    val matches = catalog.products.filter { p ->
        listOf(d.name(p.name), d.name(p.description), p.size, p.id).any { it.contains(query.trim(), true) }
    }
    if (matches.isEmpty()) Text(d.text("noResults"))
    BoxWithConstraints {
        val columns = when { maxWidth >= 900.dp -> 3; maxWidth >= 570.dp -> 2; else -> 1 }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            matches.chunked(columns).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    group.forEach { p ->
                        Card(onClick = { details(p) }, modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProductInfo(p, catalog.currency, d)
                                Text(d.text("details"), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
