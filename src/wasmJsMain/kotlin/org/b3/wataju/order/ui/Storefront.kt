package org.b3.wataju.order.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.b3.wataju.order.*
import org.b3.wataju.order.domain.*
import org.b3.wataju.order.application.Checkout
import org.b3.wataju.order.infra.*

private enum class Screen(val label: String) {
    Home("home"), Catalog("catalog"), Detail("details"), Cart("cart"), Delivery("delivery"),
    Review("review"), Complete("complete"), History("history"), Account("account")
}
private val tabs = listOf(Screen.Home, Screen.Catalog, Screen.Cart, Screen.History, Screen.Account)

// Material navigation symbols are vector paths and render independently of font coverage.
@Composable
private fun Screen.icon() = remember(this) {
    val path = when (this) {
        Screen.Home -> "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"
        Screen.Catalog -> "M3 3h8v8H3z M13 3h8v8h-8z M3 13h8v8H3z M13 13h8v8h-8z"
        Screen.Cart -> "M2 3h3l1 3h15l-3 8H8l-1 2h11v2H6l-1-2 2-4-3-7H2z M7 19h3v3H7z M16 19h3v3h-3z"
        Screen.History -> "M3 5h18v2H3z M3 11h18v2H3z M3 17h18v2H3z"
        else -> "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M4 20v-2c0-5 16-5 16 0v2z"
    }
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        addPath(pathData = PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
    }.build()
}

@Composable
fun Storefront(catalog: Catalog, config: StoreConfig, d: Display, theme: String,
               onLanguage: (String) -> Unit, onTheme: (String) -> Unit) {
    val namespace = "wataju." + config.storeId + "." + (config.orderApi ?: "preview")
    val gateway = remember(config, catalog, d.lang) {
        config.orderApi?.let { ServerOrders(it, d.lang) } ?: PreviewOrders(catalog, namespace, ::readSession, ::writeSession, ::newKey)
    }
    var quantities by remember(namespace) { mutableStateOf(reconcile(runCatching {
        json.decodeFromString<Map<String,Int>>(preference("$namespace.basket"))
    }.getOrDefault(emptyMap()), catalog)) }
    fun updateBasket(value: Map<String,Int>) {
        quantities = reconcile(value, catalog)
        savePreference("$namespace.basket", json.encodeToString(quantities))
    }
    val checkout = remember(gateway, namespace) { Checkout(gateway, TabJournal(namespace), ::newKey) }
    var pending by remember(namespace) { mutableStateOf(checkout.restore()) }
    var page by remember { mutableStateOf(if (pending == null) Screen.Home else Screen.Review) }
    var submitted by remember { mutableStateOf(readSession("$namespace.submitted").isNotEmpty() && pending != null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Product?>(null) }
    var address by remember { mutableStateOf(pending?.destination ?: Destination()) }
    var account by remember { mutableStateOf<Account?>(null) }
    var profileAddress by remember { mutableStateOf(Destination()) }
    var editingProfile by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf<List<Order>?>(null) }
    var receipt by remember { mutableStateOf<Order?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val t = d.text
    fun clearQuote() {
        checkout.clear()
        writeSession("$namespace.submitted", "")
        pending = null
        submitted = false
    }
    fun navigate(next: Screen) {
        if (busy || submitted) return
        if (page == Screen.Review && next != page) clearQuote()
        message = null
        page = next
    }
    fun errorKey(e: Exception): String = when ((e as? ResponseException)?.response?.status?.value) {
        401, 403 -> "loginRequired"
        else -> "requestFailed"
    }
    fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { message = errorKey(e) }
            finally { busy = false }
        }
    }
    LaunchedEffect(page, refresh, gateway) {
        try {
            when (page) {
                Screen.History -> { orders = null; orders = gateway.history() }
                Screen.Account -> {
                    editingProfile = false
                    account = null
                    account = gateway.account()
                    profileAddress = account!!.destination
                }
                Screen.Delivery -> if (address == Destination()) {
                    val saved = gateway.account().destination
                    if (address == Destination()) address = saved
                }
                else -> Unit
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { message = errorKey(e) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 720.dp
        Column {
            Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                Text(d.name(config.storeName), Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.headlineSmall)
            }
            if (!compact) Row { tabs.forEach { tab ->
                TextButton({ navigate(tab) }, enabled = !busy && !submitted) { Text(t(tab.label)) }
            } }
            key(page) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t(page.label), style = MaterialTheme.typography.headlineMedium)
                    if (config.orderApi == null) Text(t("demo"), color = MaterialTheme.colorScheme.secondary)
                    message?.let { msg ->
                        Text(t(msg), color = if (msg == "basketUpdated") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        if (msg == "loginRequired") config.loginUrl?.let { path -> Button({ openLogin(path) }) { Text(t("login")) } }
                        if (page in listOf(Screen.History, Screen.Account, Screen.Delivery) && msg != "basketUpdated")
                            TextButton({ message = null; refresh++ }) { Text(t("retry")) }
                    }
                    when (page) {
                        Screen.Home -> {
                            config.notices.forEach { Text(d.name(it)) }
                            Button({ navigate(Screen.Catalog) }) { Text(t("browse")) }
                            if (quantities.isNotEmpty()) OutlinedButton({ navigate(Screen.Cart) }) { Text(t("cart") + " (" + quantities.values.sum() + ")") }
                        }
                        Screen.Catalog -> Catalogue(catalog, d, query, { query = it }) { p -> selected = p; navigate(Screen.Detail) }
                        Screen.Detail -> selected?.let { p ->
                            ProductInfo(p, catalog.currency, d)
                            Text("SKU " + p.id)
                            Button({
                                updateBasket(quantities + (p.id to ((quantities[p.id] ?: 0) + 1)))
                                message = "basketUpdated"
                            }, enabled = (quantities[p.id] ?: 0) < 99) { Text(t("add")) }
                            OutlinedButton({ navigate(Screen.Cart) }) { Text(t("cart")) }
                            TextButton({ navigate(Screen.Catalog) }) { Text(t("catalog")) }
                        }
                        Screen.Cart -> Basket(catalog, quantities, d,
                            { sku, qty -> updateBasket(quantities + (sku to qty)) }, { navigate(Screen.Delivery) })
                        Screen.Delivery -> {
                            if (!busy) AddressForm(address, d) { address = it }
                            Button({ perform {
                                pending = checkout.review(quantities, address)
                                page = Screen.Review
                            } }, enabled = !busy && address.valid() && quantities.isNotEmpty()) { Text(t("review")) }
                        }
                        Screen.Review -> pending?.let { draft ->
                            Text(listOf(draft.destination.recipient, draft.destination.postal, draft.destination.address).joinToString(" · "))
                            Summary(draft.quote, d)
                            Button({ perform {
                                writeSession("$namespace.submitted", draft.key)
                                submitted = true
                                val order = try { checkout.confirm() }
                                catch (e: ResponseException) {
                                    if (e.response.status.value == 410) {
                                        clearQuote()
                                        page = Screen.Delivery
                                        message = "quoteExpired"
                                        return@perform
                                    }
                                    throw e
                                }
                                receipt = order
                                updateBasket(emptyMap())
                                clearQuote()
                                page = Screen.Complete
                            } }, enabled = !busy) { Text(t(if (submitted) "retryOrder" else "placeOrder")) }
                            if (!submitted) TextButton({ navigate(Screen.Delivery) }, enabled = !busy) { Text(t("edit")) }
                        }
                        Screen.Complete -> receipt?.let {
                            Text(t("orderNumber") + " " + it.id)
                            Summary(it.quote, d)
                            Button({ navigate(Screen.History) }) { Text(t("history")) }
                        }
                        Screen.History -> orders?.let { history ->
                            OrderHistory(history, d) { order ->
                                val available = reconcile(order.quote.lines.associate { it.sku to it.quantity }, catalog)
                                updateBasket(quantities + available.mapValues { (sku, qty) -> qty + (quantities[sku] ?: 0) })
                                navigate(Screen.Cart)
                                message = if (available.size == order.quote.lines.size) "basketUpdated" else "unavailable"
                            }
                        } ?: if (message == null) CircularProgressIndicator() else Unit
                        Screen.Account -> {
                            account?.let {
                                if (it.displayName.isNotBlank()) Text(it.displayName)
                                if (editingProfile) {
                                    if (!busy) AddressForm(profileAddress, d) { value -> profileAddress = value }
                                    Button({ perform {
                                        account = gateway.saveDestination(profileAddress)
                                        address = account!!.destination
                                        editingProfile = false
                                    } }, enabled = profileAddress.valid() && !busy) { Text(t("save")) }
                                } else {
                                    if (it.destination.valid()) Text(listOf(it.destination.recipient, it.destination.postal, it.destination.address).joinToString(" · "))
                                    OutlinedButton({ editingProfile = true }) { Text(t("edit")) }
                                }
                            }
                            Text(t("language"), style = MaterialTheme.typography.titleMedium)
                            Row { listOf("ja" to "日本語", "en" to "English").forEach { (code, label) ->
                                TextButton({ onLanguage(code) }, enabled = !busy && d.lang != code) { Text(label) }
                            } }
                            Text(t("theme"), style = MaterialTheme.typography.titleMedium)
                            Row { listOf("system","light","dark").forEach { value ->
                                TextButton({ onTheme(value) }, enabled = theme != value) { Text(t(value)) }
                            } }
                        }
                    }
                    if (busy) LinearProgressIndicator()
                }
            }
            if (compact) NavigationBar { tabs.forEach { tab ->
                NavigationBarItem(selected = tab == page, onClick = { navigate(tab) }, enabled = !busy && !submitted,
                    icon = { Icon(tab.icon(), contentDescription = null) },
                    label = { Text(t(tab.label), maxLines = 1) })
            } }
        }
    }
}
