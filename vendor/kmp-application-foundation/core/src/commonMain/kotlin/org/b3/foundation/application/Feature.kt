package org.b3.foundation.application

interface Feature<in A : Application, C : Any, T : Any> {
    val key: FeatureKey<T>

    fun configuration(): C

    fun adapt(
        application: A,
        configuration: C,
    )
}
