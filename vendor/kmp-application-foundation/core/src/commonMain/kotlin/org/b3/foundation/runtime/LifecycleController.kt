package org.b3.foundation.runtime

class LifecycleController(
    private val lifecycle: Lifecycle,
) {
    var state: LifecycleState = LifecycleState.INITIALIZED
        private set

    fun create() {
        check(state == LifecycleState.INITIALIZED)

        lifecycle.onCreate()
        state = LifecycleState.CREATED
    }

    fun start() {
        if (state == LifecycleState.CREATED || state == LifecycleState.STOPPED) {
            lifecycle.onStart()
            state = LifecycleState.STARTED
        }
    }

    fun stop() {
        if (state == LifecycleState.STARTED) {
            lifecycle.onStop()
            state = LifecycleState.STOPPED
        }
    }

    fun destroy() {
        if (state == LifecycleState.DESTROYED) return

        val wasCreated = state != LifecycleState.INITIALIZED

        try {
            stop()
        } finally {
            try {
                if (wasCreated) {
                    lifecycle.onDestroy()
                }
            } finally {
                state = LifecycleState.DESTROYED
            }
        }
    }
}
