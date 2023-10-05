package ru.alexguru.mvi_test.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

interface StateProvider<UiState> {
    val uiState: StateFlow<UiState>
}

interface EventProvider<UiEvent> {
    val uiEvent: Flow<UiEvent?>
}

interface ActionHandler<Action> {
    fun handleAction(action: Action)
}

interface ErrorHandler {
    fun handleError(throwable: Throwable)
}

class StateProviderImpl<UiState>(
    initialUiState: UiState
) : StateProvider<UiState> {
    val _uiState = MutableStateFlow(initialUiState)
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}

class EventProviderImpl<UiEvent> : EventProvider<UiEvent> {

    val _uiEvent = MutableSharedFlow<UiEvent?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val uiEvent: Flow<UiEvent?> = _uiEvent.asSharedFlow()
}

abstract class Reducer<UiState, UiEvent, Action>(
    internal val initialUiState: UiState,
    internal open val reducerScope: CoroutineScope = CoroutineScope(Job()),
    internal val stateProvider: StateProviderImpl<UiState> = StateProviderImpl(initialUiState),
    internal val eventProvider: EventProviderImpl<UiEvent> = EventProviderImpl()
) : StateProvider<UiState> by stateProvider,
    EventProvider<UiEvent> by eventProvider,
    ActionHandler<Action>,
    ErrorHandler {

    /** Точка входа со стороны domain, data действий */
    internal open var _uiState: UiState
        get() = stateProvider._uiState.value
        set(value) {
            stateProvider._uiState.value = value
        }

    /** Точка входа со стороны domain, data действий */
    internal open var _uiEvent: UiEvent?
        get() = eventProvider._uiEvent.getValueBlockedOrNull()
        set(value) {
            eventProvider._uiEvent.tryEmit(value)
        }

    /** Общий сборщик ошибок */
    override fun handleError(throwable: Throwable) {
        changeState(throwable, _uiState)
        changeEvent(throwable)
    }

    /** Сборщик действий со стороны UI */
    override fun handleAction(action: Action) {
        changeState(action, _uiState)
        changeEvent(action)
    }

    private fun changeState(throwable: Throwable, currentState: UiState) {
        val newState = calculateState(throwable, currentState)
        if (currentState != newState) {
            stateProvider._uiState.value = newState
        }
    }

    private fun changeEvent(throwable: Throwable) {
        val newEvent = calculateEvent(throwable)
        eventProvider._uiEvent.tryEmit(newEvent)
    }

    private fun changeState(action: Action, currentState: UiState) {
        val newState = calculateState(action, currentState)
        if (currentState != newState) {
            stateProvider._uiState.value = newState
        }
    }

    private fun changeEvent(action: Action) {
        val newEvent = calculateEvent(action)
        eventProvider._uiEvent.tryEmit(newEvent)
    }

    internal open fun calculateState(
        throwable: Throwable,
        currentState: UiState
    ): UiState = currentState

    internal abstract fun calculateEvent(throwable: Throwable): UiEvent?

    internal abstract fun calculateState(action: Action, currentState: UiState): UiState

    internal abstract fun calculateEvent(action: Action): UiEvent?

    protected suspend fun <T> Flow<T>.subscribe(
        onError: (Throwable) -> Unit = { handleError(it) },
        onComplete: (Throwable?) -> Unit = {},
        onNext: (T) -> Unit,
    ) = onEach { onNext(it) }
        .onCompletion { onComplete(it) }
        .catch { onError(it) }
        .collect()
}