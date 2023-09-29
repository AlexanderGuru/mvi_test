package ru.alexguru.mvi_test.core

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

interface Reducer<UiState, UiEvent, Action> {

    val uiState: StateFlow<UiState>

    val uiEvent: SharedFlow<UiEvent?>

    fun handleAction(action: Action)
}

abstract class BaseReducer<UiState, UiEvent, Action>(
    initialUiState: UiState
) : Reducer<UiState, UiEvent, Action> {

    private val _uiState = MutableStateFlow(initialUiState)
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val uiEvent: SharedFlow<UiEvent?> = _uiEvent.asSharedFlow()
//        .onEachShared { event ->
//            if (event != null) {
//                _uiEvent.tryEmit(null)
//            }
//        }

    override fun handleAction(action: Action) {
        changeState(action, _uiState.value)
        changeEvent(action)
    }

    private fun changeState(action: Action, currentState: UiState) {
        val newState = calculateState(action, currentState)
        if (currentState != newState) {
            _uiState.value = newState
        }
    }

    private fun changeEvent(action: Action) {
        val newEvent = calculateEvent(action)
        _uiEvent.tryEmit(newEvent)
    }

    protected abstract fun calculateState(action: Action, currentState: UiState): UiState

    protected abstract fun calculateEvent(action: Action): UiEvent?
}

abstract class DecoratorReducer<UiState, UiEvent, Action> constructor(
    private val dReducer: Reducer<UiState, UiEvent, Action>
) : Reducer<UiState, UiEvent, Action> {

    override val uiState: StateFlow<UiState>
        get() = dReducer.uiState
    override val uiEvent: SharedFlow<UiEvent?>
        get() = dReducer.uiEvent

    override fun handleAction(action: Action) {
        dReducer.handleAction(action)
    }
}

private fun <T> StateFlow<T>.onEachState(transform: (T) -> Unit): StateFlow<T> {
    return object : StateFlow<T> {

        override val replayCache: List<T>
            get() = this@onEachState.replayCache

        override val value: T
            get() = this@onEachState.value

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@onEachState.onEach { transform(it) }.collect(collector)
            error("StateFlow collection never ends.")
        }
    }
}

private fun <T> SharedFlow<T>.onEachShared(transform: (T) -> Unit): SharedFlow<T> {
    return object : SharedFlow<T> {

        override val replayCache: List<T>
            get() = this@onEachShared.replayCache

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            this@onEachShared.onEach { transform(it) }.collect(collector)
            error("StateFlow collection never ends.")
        }
    }
}

class LogsReducer<UiState, UiEvent, Action> constructor(
    private val dReducer: Reducer<UiState, UiEvent, Action>
) : DecoratorReducer<UiState, UiEvent, Action>(dReducer) {


    override val uiState: StateFlow<UiState> =
        super.uiState.onEachState {
            Log.d("happy", "${dReducer.javaClass.simpleName} UiState: $it")
        }


    override val uiEvent: SharedFlow<UiEvent?> =
        super.uiEvent.onEachShared {
            Log.d("happy", "${dReducer.javaClass.simpleName} UiEvent: $it")
        }

    init {
        Log.d("happy", "${dReducer.javaClass.simpleName} Initial UiState: ${uiState.value}")
    }

    override fun handleAction(action: Action) {
        Log.d("happy", "${dReducer.javaClass.simpleName} Handle action: $action")
        super.handleAction(action)
    }
}

fun <UiState, UiEvent, Action> Reducer<UiState, UiEvent, Action>.withLogs(): Reducer<UiState, UiEvent, Action> {
    return LogsReducer(this)
}

fun main() {


    val shared = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    shared.tryEmit("initialValue") // emit the initial value
    val state = shared.distinctUntilChanged() // get StateFlow-like behavior
}

@Composable
fun <T : R, R> SharedFlow<T?>.collectAsSharedState(): State<R?> = collectAsState(initial = null)
