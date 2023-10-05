package ru.alexguru.mvi_test.core.mvi.decorator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.alexguru.mvi_test.core.mvi.Reducer

abstract class DecoratorReducer<UiState, UiEvent, Action> constructor(
    private val dReducer: Reducer<UiState, UiEvent, Action>
) : Reducer<UiState, UiEvent, Action>(
    initialUiState = dReducer.initialUiState,
    reducerScope = dReducer.reducerScope,
    stateProvider = dReducer.stateProvider,
    eventProvider = dReducer.eventProvider
) {
    override var _uiState: UiState
        get() = dReducer._uiState
        set(value) {
            dReducer._uiState = value
        }
    override var _uiEvent: UiEvent?
        get() = dReducer._uiEvent
        set(value) {
            dReducer._uiEvent = value
        }

    override val uiState: StateFlow<UiState>
        get() = dReducer.uiState
    override val uiEvent: Flow<UiEvent?>
        get() = dReducer.uiEvent

    override fun handleAction(action: Action) {
        dReducer.handleAction(action)
    }

    override fun handleError(throwable: Throwable) {
        dReducer.handleError(throwable)
    }

    override suspend fun calculateState(throwable: Throwable, currentState: UiState): UiState {
        return dReducer.calculateState(throwable, currentState)
    }

    override suspend fun calculateEvent(throwable: Throwable): UiEvent? =
        dReducer.calculateEvent(throwable)

    override suspend fun calculateState(action: Action, currentState: UiState): UiState =
        dReducer.calculateState(action, currentState)

    override suspend fun calculateEvent(action: Action): UiEvent? =
        dReducer.calculateEvent(action)
}




