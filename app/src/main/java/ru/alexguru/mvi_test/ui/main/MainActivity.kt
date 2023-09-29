package ru.alexguru.mvi_test.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.alexguru.mvi_test.core.theme.Mvi_testTheme
import ru.alexguru.mvi_test.di.ServiceLocator
import ru.alexguru.mvi_test.ui.main.content.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mvi_testTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(ServiceLocator.mainViewModel)
                }
            }
        }
    }
}

