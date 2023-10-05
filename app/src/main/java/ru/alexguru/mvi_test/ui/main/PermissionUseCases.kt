package ru.alexguru.mvi_test.ui.main

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class PermissionUseCases {

    val getIndexFlow = flow<Int> {
        for (i in 0..10_000) {
            emit(i)
            delay(1000)
        }
    }
}