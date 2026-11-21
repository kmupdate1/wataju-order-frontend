package org.b3.foundation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

class ThemeConfiguration {
    var colorScheme: @Composable () -> ColorScheme = {
        if (isSystemInDarkTheme()) darkColorScheme()
        else lightColorScheme()
    }

    var shapes: Shapes = Shapes()
    var typography: Typography = Typography()
}
