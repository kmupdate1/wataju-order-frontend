package org.b3.foundation.application

internal class FeatureRegistry {
    val participants: List<ApplicationLifecycleParticipant>
        get() = entries.values
            .filterIsInstance<ApplicationLifecycleParticipant>()

    fun <T : Any> register(
        key: FeatureKey<T>,
        instance: T,
    ) {
        check(key !in entries) { "Feature  '${key.name}' already registered" }

        entries[key] = instance
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: FeatureKey<T>): T =
        checkNotNull(entries[key]) {
            " Feature '${key.name}' is not registered"
        } as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: FeatureKey<T>): T? =
        entries[key] as T?

    private val entries: MutableMap<FeatureKey<*>, Any> = linkedMapOf()
}
