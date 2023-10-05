package ru.alexguru.mvi_test.ui.main

import kotlinx.coroutines.launch
import ru.alexguru.mvi_test.core.mvi.RViewModel
import ru.alexguru.mvi_test.core.mvi.Reducer

object MainActivityContract {

    data class UiState(
        val index: Int,
        val clickCount: Int,
        val title: String
    )

    sealed class UiEvent {

        data class Toast(val message: String) : UiEvent()

        data class Dialog(val message: String) : UiEvent()
    }

    sealed class Action {
        object BtnClick : Action()
    }
}

class MainReducer constructor(
    private val useCases: PermissionUseCases
) : Reducer<MainActivityContract.UiState, MainActivityContract.UiEvent, MainActivityContract.Action>(
    MainActivityContract.UiState(0, 0,"Счетчик")
) {

    init {
        reducerScope.launch {
            useCases.getIndexFlow
                .subscribe {
                    _uiState = _uiState.copy(index = it)
                }
        }
    }

    override suspend fun calculateState(
        action: MainActivityContract.Action,
        currentState: MainActivityContract.UiState
    ): MainActivityContract.UiState =
        when (action) {
            MainActivityContract.Action.BtnClick -> currentState.copy(clickCount = currentState.clickCount + 1)
        }

    override suspend fun calculateEvent(action: MainActivityContract.Action): MainActivityContract.UiEvent =
        when (action) {
            MainActivityContract.Action.BtnClick -> MainActivityContract.UiEvent.Toast("Ты нажал на кнопку!")
        }

    override suspend fun calculateEvent(throwable: Throwable): MainActivityContract.UiEvent =
        MainActivityContract.UiEvent.Toast(throwable.message.orEmpty())

}

class MainViewModel(mainReducer: MainReducer) :
    RViewModel<MainActivityContract.UiState, MainActivityContract.UiEvent, MainActivityContract.Action>(
        reducer = mainReducer
    )