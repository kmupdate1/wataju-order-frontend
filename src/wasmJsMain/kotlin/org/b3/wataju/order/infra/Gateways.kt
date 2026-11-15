package org.b3.wataju.order.infra

import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.*
import org.b3.wataju.order.application.QuoteExpired
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.b3.wataju.order.*
import org.b3.wataju.order.application.OrderGateway
import org.b3.wataju.order.domain.*

@JsFun("() => crypto.randomUUID()")
external fun newKey(): String

@JsFun("(key) => sessionStorage.getItem(key) || ''")
external fun readSession(key: String): String
@JsFun("(key, value) => { if(value) sessionStorage.setItem(key,value); else sessionStorage.removeItem(key); }")
external fun writeSession(key: String, value: String)
@JsFun("(path) => { const u=new URL(path,document.baseURI); if(u.origin!==location.origin) throw Error('Expected same-origin API'); return u.href; }")
private external fun sameOrigin(path: String): String
@JsFun("(path) => { const u=new URL(path,document.baseURI); if(u.origin===location.origin) location.assign(u.href); }")
external fun openLogin(path: String)

@Serializable
private data class Confirmation(val quoteId: String)

class ServerOrders(base: String, private val language: String) : OrderGateway {
    private val root = sameOrigin(base).trimEnd('/')
    private suspend inline fun <reified T> read(path: String): T =
        json.decodeFromString(http.get("$root/$path") { header(HttpHeaders.AcceptLanguage, language) }.bodyAsText())

    override suspend fun account(): Account = read("me")
    override suspend fun saveDestination(destination: Destination): Account =
        json.decodeFromString(http.put("$root/me/destination") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(destination))
        }.bodyAsText())
    override suspend fun history(): List<Order> = read("orders")
    override suspend fun quote(request: QuoteRequest): Quote = json.decodeFromString(http.post("$root/quotes") {
        contentType(ContentType.Application.Json)
        header(HttpHeaders.AcceptLanguage, language)
        setBody(json.encodeToString(request))
    }.bodyAsText())

    override suspend fun place(quoteId: String, key: String): Order = try {
        json.decodeFromString(http.post("$root/orders") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", key)
            header(HttpHeaders.AcceptLanguage, language)
            setBody(json.encodeToString(Confirmation(quoteId)))
        }.bodyAsText())
    } catch (e: ResponseException) {
        if (e.response.status.value == 410) throw QuoteExpired()
        throw e
    }
}

class TabJournal(namespace: String) : org.b3.wataju.order.application.CheckoutJournal {
    private val key = "$namespace.pending"
    override fun load(): PendingOrder? = readSession(key).takeIf(String::isNotEmpty)?.let { json.decodeFromString(it) }
    override fun store(pending: PendingOrder) = writeSession(key, json.encodeToString(pending))
    override fun clear() = writeSession(key, "")
}
