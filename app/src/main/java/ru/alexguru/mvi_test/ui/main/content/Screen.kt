package ru.alexguru.mvi_test.ui.main.content

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ru.alexguru.mvi_test.core.mvi.observeWithLifecycle
import ru.alexguru.mvi_test.core.theme.Mvi_testTheme
import ru.alexguru.mvi_test.ui.main.MainActivityContract
import ru.alexguru.mvi_test.ui.main.MainReducer
import ru.alexguru.mvi_test.ui.main.MainViewModel
import ru.alexguru.mvi_test.ui.main.PermissionUseCases

@Composable
fun MainScreen(vm: MainViewModel) {
    val context = LocalContext.current
    vm.uiEvent.observeWithLifecycle { event ->
        Log.d("happy", "MainScreen: $event")
        when (event) {
            is MainActivityContract.UiEvent.Dialog -> TODO()
            is MainActivityContract.UiEvent.Toast -> Toast.makeText(
                context,
                "Btn click",
                Toast.LENGTH_SHORT
            ).show()
            null -> {}
        }
    }
    val state by vm.uiState.collectAsState()
    Column {
        Text(
            text = "Index ${state.index}!",
        )
        Text(
            text = "Click count ${state.clickCount}!",
        )
        Button(onClick = {
            vm.handleAction(MainActivityContract.Action.BtnClick)
        }) {
            Text(text = "Btn click")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val vm = MainViewModel(MainReducer(PermissionUseCases()))
    Mvi_testTheme {
        MainScreen(vm)
    }
}