package org.b3.dev.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.b3.foundation.ui.view.View

/** Behavior and Material appearance are shared; intent belongs to the consuming app. */
abstract class ExecuteButtonView(
    private val label: String,
    private val enabled: Boolean,
    private val execute: () -> Unit,
) : View {
    @Composable
    final override fun onRender() {
        Button(onClick = { if (enabled) execute() }, enabled = enabled) { Text(label) }
    }
}
