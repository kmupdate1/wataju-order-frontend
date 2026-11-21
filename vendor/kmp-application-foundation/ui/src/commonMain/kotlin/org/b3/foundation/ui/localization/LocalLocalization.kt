package org.b3.foundation.ui.localization

import androidx.compose.runtime.compositionLocalOf
import org.b3.foundation.localization.LocalizationState

internal val LocalLocalization = compositionLocalOf {
    LocalizationState.Default
}
