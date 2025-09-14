package com.github.ecasept.ccsw

import android.app.Application
import com.github.ecasept.ccsw.di.apiModule
import com.github.ecasept.ccsw.di.dataStoreModule
import com.github.ecasept.ccsw.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(dataStoreModule, apiModule, viewModelModule)
        }
    }
}