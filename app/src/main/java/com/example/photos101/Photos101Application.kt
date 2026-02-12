package com.example.photos101

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import com.example.photos101.di.networkModule
import com.example.photos101.di.repositoryModule

class Photos101Application : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Photos101Application)
            modules(appModules())
        }
    }

    private fun appModules(): List<Module> = listOf(
        networkModule,
        repositoryModule,
    )
}
