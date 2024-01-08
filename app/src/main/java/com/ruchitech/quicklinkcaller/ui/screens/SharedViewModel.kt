package com.ruchitech.quicklinkcaller.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.EventHandler


abstract class SharedViewModel : ViewModel(), EventHandler {
    //var resource = MutableStateFlow(Resource.initial<Any>())
    val showLoading = mutableStateOf(false)


    init {
        EventEmitter.subscribe(this)
    }

    infix fun EventEmitter.postEvent(event: Event) {
        postEvent(event)
    }

    override fun handleInternalEvent(event: Event) = Unit

    override fun onCleared() {
        EventEmitter.unsubscribe(this)
        super.onCleared()
    }
}