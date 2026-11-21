package org.b3.foundation.application

import org.b3.foundation.runtime.Lifecycle

abstract class Application : Lifecycle {
    override fun onCreate() {
        println("org.b3.foundation.application.Application.onCreate()")
        registry.participants
            .forEach { participant -> participant.onApplicationCreated() }
    }

    override fun onStart() {
        println("org.b3.foundation.application.Application.onStart()")
        registry.participants
            .forEach { participant -> participant.onApplicationStarted() }
    }

    override fun onStop() {
        println("org.b3.foundation.application.Application.onStop()")
        registry.participants
            .asReversed()
            .forEach { participant -> participant.onApplicationStopped() }
    }

    override fun onDestroy() {
        println("org.b3.foundation.application.Application.onDestroy()")
        registry.participants
            .asReversed()
            .forEach { participant -> participant.onApplicationDestroyed() }
    }

    fun <T : Any> register(
        key: FeatureKey<T>,
        instance: T,
    ) {
        registry.register(key, instance)
    }

    fun <T : Any> get(key: FeatureKey<T>): T = registry.get(key)
    fun <T : Any> getOrNull(key: FeatureKey<T>): T? = registry.getOrNull(key)

    private val registry: FeatureRegistry = FeatureRegistry()
}
