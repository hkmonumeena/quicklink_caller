package com.ruchitech.quicklinkcaller.helper

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log


import java.util.concurrent.CopyOnWriteArrayList

object EventEmitter {
    private val subscribers = CopyOnWriteArrayList<EventHandler>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastEventTime = 0L
    private val eventInterval = 1000L // 1 second in milliseconds
    private var lastEventMap: MutableMap<Class<out Event>, Long> = mutableMapOf()
    fun subscribe(handler: EventHandler) {
        subscribers.add(handler)
    }

    fun unsubscribe(handler: EventHandler) {
        subscribers.remove(handler)
    }

    fun postEvent(event: Event) {
        val currentTime = System.currentTimeMillis()

        if (shouldCollectEvent(event)) {
            Log.e("ghdvffgf", "postEvent: ${subscribers.size}")

            for (subscriber in subscribers) {
                subscriber.handleInternalEvent(event)
            }

            lastEventMap[event.javaClass] = currentTime
        }
    }

    private fun shouldCollectEvent(event: Event): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastEventTime = lastEventMap[event.javaClass] ?: 0L
        val eventInterval = 1000L // 1 second in milliseconds

        return currentTime - lastEventTime >= eventInterval
    }
}

interface EventHandler {
    fun handleInternalEvent(event: Event)
}

sealed class Event {
    class HomeVm(val type: Int,val any:Any? = null) : Event()
    class PermissionHandler(val type: Int,val isGranted:Boolean) : Event()
}
