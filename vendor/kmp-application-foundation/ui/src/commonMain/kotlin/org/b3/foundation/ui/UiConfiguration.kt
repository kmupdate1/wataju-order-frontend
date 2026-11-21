package org.b3.foundation.ui

import org.b3.foundation.ui.view.View
import org.b3.foundation.ui.view.ViewHierarchy

class UiConfiguration {
    fun view(factory: () -> View) {
        check(viewFactory == null) {
            "View tree is already configured"
        }

        viewFactory = factory
    }

    internal fun layer(
        hierarchy: ViewHierarchy,
        factory: (View) -> View,
    ) {
        require(hierarchy != ViewHierarchy.USER_VIEW) {
            "'USER_VIEW' is reserved for the user-defined View"
        }

        check(hierarchy !in layers) {
            "View layer '$hierarchy' is already configured"
        }

        layers[hierarchy] = factory
    }

    internal fun createView(): View {
        val view = checkNotNull(viewFactory) {
            "View tree is not configured"
        }.invoke()

        return layers.entries
            .sortedByDescending { it.key.order }
            .fold(view) { child, (_, factory) ->
                factory(child)
            }
    }


    private var viewFactory: (() -> View)? = null
    private val layers = mutableMapOf<ViewHierarchy, (View) -> View>()
}
