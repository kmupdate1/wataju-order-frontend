package org.b3.foundation.ui.view

import androidx.compose.runtime.Composable

interface ViewLayer : View {
    val children: List<View>

    @Composable
    fun Apply(content: @Composable () -> Unit) = content()

    @Composable
    override fun onRender() = Apply {
        children.forEach { child ->
            child.onRender()
        }
    }
}
