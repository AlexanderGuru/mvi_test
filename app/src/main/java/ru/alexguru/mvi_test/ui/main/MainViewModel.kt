package ru.alexguru.mvi_test.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import ru.alexguru.mvi_test.core.BaseReducer
import ru.alexguru.mvi_test.core.Reducer
import ru.alexguru.mvi_test.core.withLogs

object MainActivityContract {

    data class UiState(
        val index: Int,
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
) : BaseReducer<MainActivityContract.UiState, MainActivityContract.UiEvent, MainActivityContract.Action>(
    MainActivityContract.UiState(0, "Счетчик")
) {

//    override val initialUiState: MainActivityContract.UiState =
//        MainActivityContract.UiState(0, "Счетчик").apply {
//            Log.d("happy", ": START initialUiState")
//        }

    override fun calculateEvent(action: MainActivityContract.Action): MainActivityContract.UiEvent? =
        when (action) {
            MainActivityContract.Action.BtnClick -> MainActivityContract.UiEvent.Toast("Ты нажал на кнопку!")
        }


    override fun calculateState(
        action: MainActivityContract.Action,
        currentState: MainActivityContract.UiState
    ): MainActivityContract.UiState = currentState
//        when (action) {
//            MainActivityContract.Action.BtnClick -> currentState.copy(index = currentState.index + 1)
//        }
}

abstract class RViewModel<UiState, UiEvent, Action> constructor(
    private val reducer: Reducer<UiState, UiEvent, Action>
) : ViewModel(), Reducer<UiState, UiEvent, Action> by reducer.withLogs()

class MainViewModel(
    reducer: MainReducer
) : RViewModel<MainActivityContract.UiState, MainActivityContract.UiEvent, MainActivityContract.Action>(
    reducer
)