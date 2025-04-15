package com.my.eventtablemerger

import android.app.Application
import com.my.eventtablemerger.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class AndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin(
            platformModules = listOf(
                androidCredentialsProviderModule,
                androidModule
            )
        ) { androidContext(this@AndroidApplication) }
    }
}