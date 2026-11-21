package org.b3.foundation.ui.view

import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class ViewId private constructor(val id: Uuid) {

    override fun toString(): String = id.toString()

    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun genV7(): ViewId = ViewId(id = Uuid.generateV7())
    }
}
