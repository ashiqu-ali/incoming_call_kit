package com.ashiquali.incoming_call_kit

object CallKitEventBus {
    private val listeners =
        mutableListOf<(action: String, callId: String, extra: Map<String, Any?>?) -> Unit>()

    fun register(listener: (action: String, callId: String, extra: Map<String, Any?>?) -> Unit) {
        listeners.add(listener)
    }

    fun unregister(listener: (action: String, callId: String, extra: Map<String, Any?>?) -> Unit) {
        listeners.remove(listener)
    }

    fun hasListeners(): Boolean = listeners.isNotEmpty()

    fun emit(action: String, callId: String, extra: Map<String, Any?>? = null) {
        listeners.toList().forEach { it(action, callId, extra) }
    }
}
