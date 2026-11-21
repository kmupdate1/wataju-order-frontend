package org.b3.wataju.order.ui

import org.b3.dev.ui.ExecuteButtonView

class AddToCartButtonView(label: String, enabled: Boolean, add: () -> Unit) :
    ExecuteButtonView(label, enabled, add)
