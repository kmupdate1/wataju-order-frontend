package org.b3.wataju.order

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.platform.Font
import io.ktor.client.call.body
import io.ktor.client.request.get
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import org.b3.wataju.shared.ui.*
import org.b3.wataju.order.ui.*
import kotlinx.browser.document
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) { Store() }
}

@Composable
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun Store() {
    var config by remember { mutableStateOf<StoreConfig?>(null) }
    var lang by remember { mutableStateOf(preference("wataju.locale").takeIf { it in listOf("ja","en") }
        ?: browserLanguage().substringBefore('-').lowercase()) }
    var theme by remember { mutableStateOf(preference("wataju.theme").takeIf { it in listOf("dark","light") } ?: "system") }
    var attempt by remember { mutableIntStateOf(0) }
    var strings by remember { mutableStateOf(emptyMap<String,String>()) }
    var catalog by remember { mutableStateOf<Catalog?>(null) }
    var family by remember { mutableStateOf<FontFamily?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(lang, attempt) {
        failed = false
        catalog = null
        try {
            val settings = loadConfig()
            config = settings
            if (lang !in listOf("ja","en")) { lang = settings.defaultLocale; return@LaunchedEffect }
            if (family == null) {
                val bytes: ByteArray = http.get(resolveUrl("fonts/NotoSansJP.ttf")).body()
                family = FontFamily(Font("NotoSansJP", bytes, variationSettings = FontVariation.Settings(FontVariation.weight(400))))
            }
            strings = loadStrings(lang)
            setPage(lang, settings.storeName.localized(lang, settings.defaultLocale))
            catalog = loadCatalog(settings, lang)

        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { failed = true }
    }

    val dark = theme == "dark" || (theme == "system" && isSystemInDarkTheme())
    val typography = remember(family) { storeTypography(family) }
    MaterialTheme(colorScheme = if (dark) DarkPalette else LightPalette, typography = typography) {
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
                Storefront(items, config!!, Display(strings::getValue, lang, config!!.defaultLocale), theme,
                    onLanguage = { lang = it; savePreference("wataju.locale", it) },
                    onTheme = { theme = it; savePreference("wataju.theme", it) })
            }
        }
    }
}
