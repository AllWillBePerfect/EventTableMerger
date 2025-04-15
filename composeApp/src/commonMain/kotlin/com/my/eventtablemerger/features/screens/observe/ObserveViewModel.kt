package com.my.eventtablemerger.features.screens.observe

import com.my.eventtablemerger.core.base.BaseViewModel

class ObserveViewModel : BaseViewModel<ObserveViewState, ObserveAction, ObserveEvent>(initialState = ObserveViewState()) {


    override fun obtainEvent(viewEvent: ObserveEvent) {
    }

    override fun dispatch(action: ObserveAction) {

    }

}

class ObserveViewState {}
class ObserveEvent {}
class ObserveAction {}