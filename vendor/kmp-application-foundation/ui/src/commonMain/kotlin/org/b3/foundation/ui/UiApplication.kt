package org.b3.foundation.ui

import androidx.compose.runtime.Composable
import org.b3.foundation.application.Application
import org.b3.foundation.ui.view.View

object UiApplication : Application() {
    val view: View
        get() = checkNotNull(getOrNull(UI.key)) { "UI is not configured" }

    @Composable
    fun Content() {
        view.onRender()
    }

    override fun onCreate() {
        super.onCreate()
        println("org.b3.foundation.ui.UiApplication.onCreate()")
    }

    override fun onStart() {
        super.onStart()
        println("org.b3.foundation.ui.UiApplication.onStart()")
    }

    override fun onStop() {
        super.onStop()
        println("org.b3.foundation.ui.UiApplication.onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("org.b3.foundation.ui.UiApplication.onDestroy()")
    }
}
