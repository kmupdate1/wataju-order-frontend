package org.b3.foundation.ui.surface

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class SurfaceConfiguration {
    var modifier: Modifier = Modifier.fillMaxSize()
    var color: Color? = null
    var contentColor: Color? = null
    var isCentered: Boolean = false
}
