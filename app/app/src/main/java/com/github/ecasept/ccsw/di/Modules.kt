package com.github.ecasept.ccsw.di


import com.github.ecasept.ccsw.data.preferences.PDSRepo
import com.github.ecasept.ccsw.data.preferences.dataStore
import com.github.ecasept.ccsw.network.ApiClient
import com.github.ecasept.ccsw.network.createAPI
import com.github.ecasept.ccsw.network.createOkHttpClient
import com.github.ecasept.ccsw.network.createRetrofit
import com.github.ecasept.ccsw.ui.screens.home.HomeViewModel
import com.github.ecasept.ccsw.ui.screens.login.LoginViewModel
import com.github.ecasept.ccsw.ui.screens.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val dataStoreModule = module {
    // DataStore
    single { androidContext().dataStore }
    // DataStore Repository
    single {
        PDSRepo(get())
    }
}

val apiModule = module {
    // OkHttpClient
    single {
        createOkHttpClient(get())
    }
    // Retrofit
    single {
        createRetrofit(get())
    }
    // API Service
    single {
        createAPI(get())
    }
    // Api Client
    single {
        ApiClient(get(), get())
    }
}

val viewModelModule = module {
    viewModel {
        LoginViewModel(get(), get())
    }
    viewModel {
        HomeViewModel(get(), get())
    }
    viewModel {
        SettingsViewModel(get())
    }
}