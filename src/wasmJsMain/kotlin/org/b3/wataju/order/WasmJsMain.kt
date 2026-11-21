package org.b3.wataju.order

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.b3.foundation.application.ApplicationLifecycleParticipant
import org.b3.foundation.application.FeatureKey
import org.b3.foundation.application.install
import org.b3.foundation.localization.Localization
import org.b3.foundation.localization.LocalizationState
import org.b3.foundation.runtime.LifecycleController
import org.b3.foundation.ui.UI
import org.b3.foundation.ui.UiApplication
import org.b3.foundation.ui.localization
import org.w3c.dom.events.Event

/** Platform entry is deliberately thin; lifecycle transitions belong to foundation. */
fun interface Bootable { fun boot() }

object WasmJsMain : Bootable {
    private var booted = false

    @OptIn(ExperimentalComposeUiApi::class)
    override fun boot() {
        check(!booted) { "The browser application is already booted" }
        booted = true
        val fallback = runCatching { loadConfig().defaultLocale }.getOrDefault("ja")
        val language = storeLanguages.select(preference("wataju.locale"), browserLanguage(), fallback)
        UiApplication.install(Localization) {
            this.language = if (language == "ja") LocalizationState.JA else LocalizationState.EN
        }
        UiApplication.register(FeatureKey<ApplicationLifecycleParticipant>("StoreHttp"), object : ApplicationLifecycleParticipant {
            override fun onApplicationDestroyed() { http.close() }
        })
        UiApplication.install(UI) {
            localization()
            view { StoreApplicationView() }
        }
        val lifecycle = LifecycleController(UiApplication)
        lifecycle.create()
        ComposeViewport(document.body!!) {
            DisposableEffect(lifecycle) {
                // pagehide/pageshow includes bfcache. Hidden tabs stop, restored tabs restart.
                val visibility: (Event) -> Unit = {
                    if (documentHidden()) lifecycle.stop() else lifecycle.start()
                }
                val hide: (Event) -> Unit = { lifecycle.stop() }
                val show: (Event) -> Unit = { if (!documentHidden()) lifecycle.start() }
                document.addEventListener("visibilitychange", visibility)
                window.addEventListener("pagehide", hide)
                window.addEventListener("pageshow", show)
                if (!documentHidden()) lifecycle.start()
                onDispose {
                    document.removeEventListener("visibilitychange", visibility)
                    window.removeEventListener("pagehide", hide)
                    window.removeEventListener("pageshow", show)
                    lifecycle.destroy()
                }
            }
            UiApplication.Content()
        }
    }
}

@JsFun("() => document.hidden")
private external fun documentHidden(): Boolean
