package ru.alexguru.mvi_test.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.alexguru.mvi_test.core.mvi.decorator.withLogs
import ru.alexguru.mvi_test.core.mvi.decorator.withSafeAsync

abstract class RViewModel<UiState, UiEvent, Action> constructor(
    reducer: Reducer<UiState, UiEvent, Action>
) : ViewModel(),
    StateProvider<UiState>,
    EventProvider<UiEvent>,
    ActionHandler<Action>,
    ErrorHandler {

    private val _reducer = reducer
        .withLogs()
        .withSafeAsync(viewModelScope)

    override val uiState: StateFlow<UiState> = _reducer.uiState

    override val uiEvent: Flow<UiEvent?> = _reducer.uiEvent

    override fun handleAction(action: Action) = _reducer.handleAction(action)

    override fun handleError(throwable: Throwable) = _reducer.handleError(throwable)
}
