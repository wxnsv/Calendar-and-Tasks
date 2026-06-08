package com.nikkap.calendar.app.core.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            workManagerFactory()
            modules(networkModule, authModule, appModule, localModule)
        }
    }
}