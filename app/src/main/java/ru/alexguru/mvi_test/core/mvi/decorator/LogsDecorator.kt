package ru.alexguru.mvi_test.core.mvi.decorator

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import ru.alexguru.mvi_test.core.mvi.Reducer
import ru.alexguru.mvi_test.core.mvi.onEachState

class LogsDecorator<UiState, UiEvent, Action> constructor(
    dReducer: Reducer<UiState, UiEvent, Action>
) : DecoratorReducer<UiState, UiEvent, Action>(dReducer) {

    private val reducerName = dReducer.javaClass.simpleName

    override var _uiState: UiState
        get() = super._uiState
        set(value) {
            Log.d("happy", "$reducerName Domain/Data -> Change UiState: $value")
            super._uiState = value
        }
    override var _uiEvent: UiEvent?
        get() = super._uiEvent
        set(value) {
            Log.d("happy", "$reducerName Domain/Data -> Change UiEvent: $value")
            super._uiEvent = value
        }

    override val uiState: StateFlow<UiState> =
        super.uiState.onEachState {
            Log.d("happy", "$reducerName UiState: $it")
        }


    override val uiEvent: Flow<UiEvent?> =
        super.uiEvent.onEach {
            Log.d("happy", "$reducerName UiEvent: $it")
        }

    init {
        Log.d("happy", "$reducerName Initial UiState: ${uiState.value}")
    }

    override fun handleAction(action: Action) {
        Log.d("happy", "$reducerName UI -> Handle action: $action")
        super.handleAction(action)
    }

    override fun handleError(throwable: Throwable) {
        Log.d("happy", "$reducerName Handle error: $throwable")
        super.handleError(throwable)
    }
}

fun <UiState, UiEvent, Action> Reducer<UiState, UiEvent, Action>.withLogs(): Reducer<UiState, UiEvent, Action> =
    LogsDecorator(this)