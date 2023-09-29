package ru.alexguru.mvi_test.di

import ru.alexguru.mvi_test.ui.main.MainReducer
import ru.alexguru.mvi_test.ui.main.MainViewModel
import ru.alexguru.mvi_test.ui.main.PermissionUseCases

object ServiceLocator {

    val permissionUseCases by lazy { PermissionUseCases() }

    val mainReducer by lazy { MainReducer(permissionUseCases) }

    val mainViewModel by lazy { MainViewModel(mainReducer) }
}