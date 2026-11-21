package org.b3.foundation.application

interface ApplicationLifecycleParticipant {
    fun onApplicationCreated() {}
    fun onApplicationStarted() {}
    fun onApplicationStopped() {}
    fun onApplicationDestroyed() {}
}
