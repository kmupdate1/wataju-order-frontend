package org.b3.wataju.integration

import kotlin.test.*
import org.b3.foundation.application.*
import org.b3.foundation.runtime.*

class FoundationLifecycleTest {
    @Test fun applicationFeaturesStartInOrderAndStopInReverse() {
        val events = mutableListOf<String>()
        val app = object : Application() {}
        fun participant(name: String) = object : ApplicationLifecycleParticipant {
            override fun onApplicationCreated() { events += "$name.create" }
            override fun onApplicationStarted() { events += "$name.start" }
            override fun onApplicationStopped() { events += "$name.stop" }
            override fun onApplicationDestroyed() { events += "$name.destroy" }
        }
        app.register(FeatureKey("http"), participant("http"))
        app.register(FeatureKey("store"), participant("store"))
        val lifecycle = LifecycleController(app)
        lifecycle.create()
        lifecycle.start()
        lifecycle.start() // Recomposition / duplicate pageshow must not restart services.
        lifecycle.stop()
        lifecycle.stop()
        lifecycle.start() // bfcache / visible tab resumes.
        lifecycle.destroy()
        lifecycle.destroy()
        lifecycle.start() // A disposed application cannot resume.
        assertEquals(listOf("http.create", "store.create", "http.start", "store.start",
            "store.stop", "http.stop", "http.start", "store.start", "store.stop", "http.stop",
            "store.destroy", "http.destroy"), events)
        assertEquals(LifecycleState.DESTROYED, lifecycle.state)
    }

    @Test fun featureKeysPreserveTypeIdentityAndRejectDuplicateInstall() {
        val app = object : Application() {}
        val key = FeatureKey<String>("locale")
        app.register(key, "ja")
        assertEquals("ja", app.get(key))
        assertNull(app.getOrNull(FeatureKey<String>("locale")))
        assertFailsWith<IllegalStateException> { app.register(key, "en") }
    }

    @Test fun destroyAlwaysFinalizesStateEvenWhenStopFails() {
        var destroyed = false
        val lifecycle = LifecycleController(object : Lifecycle {
            override fun onCreate() {}
            override fun onStart() {}
            override fun onStop() { error("stop failed") }
            override fun onDestroy() { destroyed = true }
        })
        lifecycle.create(); lifecycle.start()
        assertFailsWith<IllegalStateException> { lifecycle.destroy() }
        assertTrue(destroyed)
        assertEquals(LifecycleState.DESTROYED, lifecycle.state)
    }
}
