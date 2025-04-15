package com.my.eventtablemerger.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<State : Any, Action, Event>(initialState: State) : ViewModel() {
    private val _viewState = MutableStateFlow(initialState)

    fun viewState(): StateFlow<State> = _viewState.asStateFlow()

    protected var viewState: State
        get() = _viewState.value
        set(value) {
            _viewState.value = value
        }

    protected fun setState(reduce: State.() -> State) {
        val newState = viewState.reduce()
        _viewState.value = newState
    }

    abstract fun obtainEvent(viewEvent: Event)
    abstract fun dispatch(action: Action)
}