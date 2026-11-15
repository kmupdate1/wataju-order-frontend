package org.b3.wataju.order

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

internal val http = HttpClient {
    expectSuccess = true
    install(HttpTimeout) { requestTimeoutMillis = 15000 }
}
internal val json = Json { ignoreUnknownKeys = true }

@JsFun("(path) => { const u = new URL(path, document.baseURI); if (!['http:', 'https:'].includes(u.protocol)) throw new Error('URL'); return u.href; }")
internal external fun resolveUrl(path: String): String

@JsFun("(key) => { try { return localStorage.getItem(key) || ''; } catch { return ''; } }")
external fun preference(key: String): String
@JsFun("(key, value) => { try { localStorage.setItem(key, value); } catch {} }")
external fun savePreference(key: String, value: String)
@JsFun("() => navigator.language")
external fun browserLanguage(): String
@JsFun("(lang, title) => { document.documentElement.lang = lang; document.title = title; }")
external fun setPage(lang: String, title: String)
@JsFun("(value, currency, lang) => new Intl.NumberFormat(lang, {style:'currency', currency}).format(value)")
external fun formatMoney(value: Double, currency: String, lang: String): String

@JsFun("() => window.watajuConfig")
private external fun runtimeConfig(): String

fun loadConfig(): StoreConfig =
    json.decodeFromString<StoreConfig>(runtimeConfig()).also {
        require(it.catalogUrl.isNotBlank() && it.defaultLocale in listOf("en", "ja"))
    }

suspend fun loadStrings(lang: String): Map<String, String> =
    json.decodeFromString(http.get(resolveUrl("locales/$lang.json")).bodyAsText())

suspend fun loadCatalog(config: StoreConfig, lang: String): Catalog =
    json.decodeFromString<Catalog>(http.get(resolveUrl(config.catalogUrl)) {
        header(HttpHeaders.AcceptLanguage, lang)
    }.bodyAsText()).validated()
