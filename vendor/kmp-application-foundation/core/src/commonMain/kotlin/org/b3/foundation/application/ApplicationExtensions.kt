package org.b3.foundation.application

import org.b3.foundation.annotation.B3Dsl

fun <A : Application, C : Any, T : Any> A.install(
    feature: Feature<A, C, T>,
    configure: @B3Dsl C.() -> Unit = {},
): A = this.also { application ->
    val configuration = feature.configuration()
        .apply(configure)

    feature.adapt(application, configuration)
}
