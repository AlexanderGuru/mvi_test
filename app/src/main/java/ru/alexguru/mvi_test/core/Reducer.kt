package ru.alexguru.mvi_test.core

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface Reducer<UiState, UiEvent, Action> {

    val uiState: StateFlow<UiState>

    val uiEvent: Flow<UiEvent?>

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
    override val uiEvent: Flow<UiEvent?> = _uiEvent.asSharedFlow()

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
    override val uiEvent: Flow<UiEvent?>
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


    override val uiEvent: Flow<UiEvent?> =
        super.uiEvent.onEach {
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

@Composable
inline fun <reified T> Flow<T>.observeWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        lifecycleOwner.lifecycleScope.launch {
            flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(action)
        }
    }
}