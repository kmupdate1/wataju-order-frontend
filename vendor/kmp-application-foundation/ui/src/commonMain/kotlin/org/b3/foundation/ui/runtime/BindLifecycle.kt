package org.b3.foundation.ui.runtime

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleStartEffect
import org.b3.foundation.runtime.LifecycleController

@Composable
fun BindLifecycle(
    controller: LifecycleController,
) {
    LifecycleStartEffect(controller) {
        controller.start()
        onStopOrDispose { controller.stop() }
    }
}
