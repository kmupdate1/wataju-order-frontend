package org.b3.wataju.order

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) { Store() }
}

@Composable
private fun Store() {
    var config by remember { mutableStateOf<StoreConfig?>(null) }
    var lang by remember { mutableStateOf(preference("wataju.locale").takeIf { it in listOf("ja","en") }
        ?: browserLanguage().substringBefore('-').lowercase()) }
    var theme by remember { mutableStateOf(preference("wataju.theme").takeIf { it in listOf("dark","light") } ?: "system") }
    var attempt by remember { mutableIntStateOf(0) }
    var strings by remember { mutableStateOf(emptyMap<String,String>()) }
    var catalog by remember { mutableStateOf<Catalog?>(null) }
    var failed by remember { mutableStateOf(false) }
    val cart = remember { mutableStateMapOf<String,Int>() }
    var page by remember { mutableStateOf("catalog") }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(lang, attempt) {
        failed = false
        catalog = null
        try {
            val settings = loadConfig()
            config = settings
            if (lang !in listOf("ja","en")) { lang = settings.defaultLocale; return@LaunchedEffect }
            strings = loadStrings(lang)
            setPage(lang, settings.storeName.localized(lang, settings.defaultLocale))
            catalog = loadCatalog(settings, lang)
            cart.keys.retainAll(catalog!!.products.map { it.id }.toSet())
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { failed = true }
    }

    val dark = theme == "dark" || (theme == "system" && isSystemInDarkTheme())
    MaterialTheme(colorScheme = if (dark) DarkPalette else LightPalette) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val items = catalog
            if (items == null) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (failed) {
                        Text(strings["error"] ?: if (lang == "en") "Could not open the store." else "店舗を開けませんでした。")
                        Button({ attempt++ }) { Text(strings["retry"] ?: if (lang == "en") "Retry" else "再読み込み") }
                    } else CircularProgressIndicator()
                }
            } else {
                val t: (String) -> String = { key -> strings.getValue(key) }
                val fallback = config!!.defaultLocale
                val price = { n: Long -> formatMoney(n.toDouble(), items.currency, lang) }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(config!!.storeName.localized(lang, fallback), style = MaterialTheme.typography.headlineLarge)
                    Row {
                        TextButton({ lang = if (lang == "ja") "en" else "ja"; savePreference("wataju.locale", lang) }) {
                            Text(if (lang == "ja") "English" else "日本語")
                        }
                        TextButton({
                            theme = when (theme) { "system" -> "light"; "light" -> "dark"; else -> "system" }
                            savePreference("wataju.theme", theme)
                        }) { Text(t(theme)) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton({ page = "catalog" }) { Text(t("catalog")) }
                        OutlinedButton({ page = "cart" }) { Text(t("cart") + " (" + cart.values.sum() + ")") }
                    }
                    HorizontalDivider()
                    Text(t(page), style = MaterialTheme.typography.headlineMedium)
                    if (page == "catalog") {
                        OutlinedTextField(query, { query = it }, label = { Text(t("search")) }, singleLine = true)
                        val matches = items.products.filter {
                            listOf(it.name.localized(lang, fallback), it.description.localized(lang, fallback), it.size)
                                .any { field -> field.contains(query.trim(), ignoreCase = true) }
                        }
                        if (matches.isEmpty()) Text(t("noResults"))
                        BoxWithConstraints {
                            val columns = when { maxWidth >= 900.dp -> 3; maxWidth >= 600.dp -> 2; else -> 1 }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                matches.chunked(columns).forEach { group ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        group.forEach { p ->
                                            Card(Modifier.weight(1f)) {
                                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(p.name.localized(lang, fallback), style = MaterialTheme.typography.titleLarge)
                                                    Text(p.description.localized(lang, fallback))
                                                    Text(p.size + " · " + price(p.unitPrice))
                                                    Button({ cart[p.id] = (cart[p.id] ?: 0) + 1 }, enabled = (cart[p.id] ?: 0) < 99) { Text(t("add")) }
                                                }
                                            }
                                        }
                                        repeat(columns - group.size) { Spacer(Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    } else {
                        val lines = items.products.filter { it.id in cart }
                        if (lines.isEmpty()) Text(t("empty"))
                        else {
                            lines.forEach { p ->
                                val qty = cart.getValue(p.id)
                                Text(p.name.localized(lang, fallback) + " · " + p.size, style = MaterialTheme.typography.titleMedium)
                                Text(price(p.unitPrice) + " × " + qty + " = " + price(p.unitPrice * qty))
                                if (page == "cart") Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton({ cart[p.id] = qty - 1 }, Modifier.semantics { contentDescription = t("decrease") }, enabled = qty > 1) { Text("−") }
                                    TextButton({ cart[p.id] = qty + 1 }, Modifier.semantics { contentDescription = t("increase") }, enabled = qty < 99) { Text("+") }
                                    TextButton({ cart.remove(p.id) }) { Text(t("remove")) }
                                }
                                HorizontalDivider()
                            }
                            Text(t("subtotal") + "  " + price(lines.sumOf { it.unitPrice * cart.getValue(it.id) }))
                            Text(t("estimate"))
                            if (page == "review") Text(t("reviewNote"))
                            Button({ page = if (page == "cart") "review" else "cart" }) {
                                Text(t(if (page == "cart") "review" else "back"))
                            }
                        }
                    }
                }
            }
        }
    }
}
