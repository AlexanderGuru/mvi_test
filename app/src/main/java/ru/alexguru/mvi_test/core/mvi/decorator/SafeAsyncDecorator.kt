package ru.alexguru.mvi_test.core.mvi.decorator

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import ru.alexguru.mvi_test.core.mvi.Reducer

class SafeAsyncDecorator<UiState, UiEvent, Action>(
    private val cp: CoroutineScope,
    dReducer: Reducer<UiState, UiEvent, Action>
) : DecoratorReducer<UiState, UiEvent, Action>(dReducer) {

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            handleError(throwable)
        }
    override val reducerScope: CoroutineScope
        get() = cp + exceptionHandler
}

fun <UiState, UiEvent, Action> Reducer<UiState, UiEvent, Action>.withSafeAsync(
    cp: CoroutineScope
): Reducer<UiState, UiEvent, Action> = SafeAsyncDecorator(cp, this)