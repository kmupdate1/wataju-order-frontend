package org.b3.wataju.order.ui

import org.b3.dev.ui.AdaptiveGridView
import org.b3.foundation.ui.view.View
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

class ProductImageView(private val product: Product, private val d: Display) : View {
    @Composable
    override fun onRender() {
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
}

class ProductInfoView(private val p: Product, private val currency: String, private val d: Display) : View {
    @Composable
    override fun onRender() {
        ProductImageView(p, d).onRender()
        Text(d.name(p.name), style = MaterialTheme.typography.titleLarge)
        Text(d.name(p.description))
        Text(p.size + " · " + d.money(p.unitPrice, currency))
    }
}

class CatalogueView(private val catalog: Catalog, private val d: Display, private val query: String, private val search: (String) -> Unit, private val details: (Product) -> Unit) : View {
    @Composable
    override fun onRender() {
        OutlinedTextField(query, search, label = { Text(d.text("search")) }, singleLine = true)
        val matches = catalog.products.filter { p ->
            listOf(d.name(p.name), d.name(p.description), p.size, p.id).any { it.contains(query.trim(), true) }
        }
        if (matches.isEmpty()) Text(d.text("noResults"))
        AdaptiveGridView(matches, columnsFor = { width ->
            when { width >= 900.dp -> 3; width >= 570.dp -> 2; else -> 1 }
        }, cell = { product, modifier ->
            Card(onClick = { details(product) }, modifier = modifier) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProductInfoView(product, catalog.currency, d).onRender()
                    Text(d.text("details"), color = MaterialTheme.colorScheme.primary)
                }
            }
        }).onRender()
    }
}
