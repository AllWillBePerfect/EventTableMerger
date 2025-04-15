package com.my.eventtablemerger.core.di

import com.my.eventtablemerger.features.screens.observe.ObserveViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
    platformModules: List<Module> = emptyList(),
    platformAction: KoinApplication.() -> Unit
) = startKoin {
    appDeclaration()

    modules(
        commonModule +
                viewModelModule +
                platformModules
    )
    platformAction.invoke(this)
}

val commonModule = module {
}

val viewModelModule = module {
    viewModelOf(::ObserveViewModel)

}