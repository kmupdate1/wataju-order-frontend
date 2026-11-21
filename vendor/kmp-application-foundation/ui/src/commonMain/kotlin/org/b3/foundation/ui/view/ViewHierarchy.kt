package org.b3.foundation.ui.view

enum class ViewHierarchy(val order: Int) {
    LOCALIZATION(100),
    THEME(200),
    SURFACE(300),
    OVERLAY_HOST(400),
    USER_VIEW(999),
}
