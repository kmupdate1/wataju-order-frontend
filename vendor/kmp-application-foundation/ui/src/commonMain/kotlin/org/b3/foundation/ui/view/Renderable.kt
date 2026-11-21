package org.b3.foundation.ui.view

import androidx.compose.runtime.Composable

interface Renderable {
    @Composable
    fun onRender()
}
