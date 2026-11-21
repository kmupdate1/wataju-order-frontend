package org.b3.wataju.order.ui

import org.b3.dev.ui.AdaptiveNavigationView
import org.b3.foundation.ui.view.View
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
import org.b3.wataju.order.*
import org.b3.wataju.order.domain.*
import org.b3.wataju.order.application.*
import org.b3.wataju.order.infra.*

private enum class Screen(val label: String) {
    Home("home"), Catalog("catalog"), Detail("details"), Cart("cart"), Delivery("delivery"),
    Review("review"), Complete("complete"), History("history"), Account("account")
}
private val tabs = listOf(Screen.Home, Screen.Catalog, Screen.Cart, Screen.History, Screen.Account)

// Material navigation symbols are vector paths and render independently of font coverage.
private fun Screen.icon(): ImageVector {
    val path = when (this) {
        Screen.Home -> "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"
        Screen.Catalog -> "M3 3h8v8H3z M13 3h8v8h-8z M3 13h8v8H3z M13 13h8v8h-8z"
        Screen.Cart -> "M2 3h3l1 3h15l-3 8H8l-1 2h11v2H6l-1-2 2-4-3-7H2z M7 19h3v3H7z M16 19h3v3h-3z"
        Screen.History -> "M3 5h18v2H3z M3 11h18v2H3z M3 17h18v2H3z"
        else -> "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M4 20v-2c0-5 16-5 16 0v2z"
    }
    return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        addPath(pathData = PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
    }.build()
}

class StorefrontShell(
    private val catalog: Catalog,
    private val config: StoreConfig,
    private val services: StoreServices,
    private val d: Display,
    private val theme: () -> String,
    private val onLanguage: (String) -> Unit,
    private val onTheme: (String) -> Unit,
) : View {
    @Composable
    override fun onRender() {
        val gateway = services.orders
        val controller = services.checkout
        var quantities by remember(services) { mutableStateOf(services.basket.load()) }
        fun updateBasket(value: Map<String, Int>) { quantities = services.basket.save(value) }
        var orderState by remember(controller) { mutableStateOf(controller.state) }
        val pending = when (val state = orderState) {
            is OrderState.Reviewing -> state.draft
            is OrderState.AwaitingReceipt -> state.draft
            else -> null
        }
        val submitted = orderState is OrderState.AwaitingReceipt
        val receipt = (orderState as? OrderState.Complete)?.order
        var page by remember { mutableStateOf(if (pending == null) Screen.Home else Screen.Review) }
        var busy by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        var query by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf<Product?>(null) }
        var address by remember { mutableStateOf(pending?.destination ?: Destination()) }
        var account by remember { mutableStateOf<Account?>(null) }
        var profileAddress by remember { mutableStateOf(Destination()) }
        var editingProfile by remember { mutableStateOf(false) }
        var orders by remember { mutableStateOf<List<Order>?>(null) }
        var refresh by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()
        val t = d.text
        fun navigate(next: Screen) {
            if (busy || submitted) return
            if (page == Screen.Review && next != page) {
                controller.edit()
                orderState = controller.state
            }
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
                finally { orderState = controller.state; busy = false }
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
        val selectedTab = when (page) {
            Screen.Detail -> Screen.Catalog
            Screen.Delivery, Screen.Review, Screen.Complete -> Screen.Cart
            else -> page
        }
        AdaptiveNavigationView(
            destinations = tabs, selected = selectedTab, enabled = !busy && !submitted,
            onSelect = ::navigate, label = { t(it.label) },
            icon = { Icon(remember(it) { it.icon() }, contentDescription = null) },
            header = {
                Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(d.name(config.storeName), Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                }
            },
            content = {
            key(page) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
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
                        Screen.Home -> HomeScreenView(config, d, quantities.values.sum(),
                            { navigate(Screen.Catalog) }, { navigate(Screen.Cart) }).onRender()
                        Screen.Catalog -> CatalogueView(catalog, d, query, { query = it }, { p -> selected = p; navigate(Screen.Detail) }).onRender()
                        Screen.Detail -> selected?.let { p ->
                            ProductDetailScreenView(p, catalog.currency, d, (quantities[p.id] ?: 0) < 99,
                                add = {
                                    updateBasket(quantities + (p.id to ((quantities[p.id] ?: 0) + 1)))
                                    message = "basketUpdated"
                                }, cart = { navigate(Screen.Cart) }, back = { navigate(Screen.Catalog) }).onRender()
                        }
                        Screen.Cart -> BasketView(catalog, quantities, d,
                            { sku, qty -> updateBasket(quantities + (sku to qty)) }, { navigate(Screen.Delivery) }).onRender()
                        Screen.Delivery -> DeliveryScreenView(address, d, busy, quantities.isNotEmpty(),
                            edit = { address = it }, review = { perform {
                                controller.review(quantities, address)
                                page = Screen.Review
                            } }).onRender()
                        Screen.Review -> pending?.let { draft ->
                            ReviewScreenView(draft, d, busy, submitted, confirm = { perform {
                                try { controller.confirm() }
                                catch (_: QuoteExpired) {
                                    page = Screen.Delivery
                                    message = "quoteExpired"
                                    return@perform
                                }
                                updateBasket(emptyMap())
                                page = Screen.Complete
                            } }, edit = { navigate(Screen.Delivery) }).onRender()
                        }
                        Screen.Complete -> receipt?.let {
                            CompleteScreenView(it, d, { navigate(Screen.History) }).onRender()
                        }
                        Screen.History -> orders?.let { history ->
                            OrderHistoryView(history, d, { order ->
                                val available = reconcile(order.quote.lines.associate { it.sku to it.quantity }, catalog)
                                updateBasket(quantities + available.mapValues { (sku, qty) -> qty + (quantities[sku] ?: 0) })
                                navigate(Screen.Cart)
                                message = if (available.size == order.quote.lines.size) "basketUpdated" else "unavailable"
                            }).onRender()
                        } ?: if (message == null) CircularProgressIndicator() else Unit
                        Screen.Account -> AccountScreenView(account, profileAddress, editingProfile,
                            busy || submitted, d, theme(), edit = { profileAddress = it },
                            beginEdit = { editingProfile = true }, save = { perform {
                                account = gateway.saveDestination(profileAddress)
                                address = account!!.destination
                                editingProfile = false
                            } }, language = onLanguage, appearance = onTheme).onRender()
                    }
                    if (busy) LinearProgressIndicator()
                }
            }
            },
        ).onRender()
    }
}
