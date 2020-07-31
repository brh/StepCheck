package com.brh.stepcheck.model

/**
 * State Classes to reflect state of app.
 */
sealed class State {
    object Loading:State()
    class Content(val steps : List<StepData>) : State()
    class Error(val message : Throwable) : State()
}