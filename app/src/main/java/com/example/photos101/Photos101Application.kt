package com.example.photos101

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import com.example.photos101.di.localModule
import com.example.photos101.di.networkModule
import com.example.photos101.di.repositoryModule
import com.example.photos101.di.useCaseModule
import com.example.photos101.di.viewModelModule
import com.example.photos101.di.workManagerModule
import com.example.photos101.worker.PhotosPollWorker
import org.koin.androidx.workmanager.koin.workManagerFactory

class Photos101Application : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Photos101Application)
            workManagerFactory()
            modules(appModules())
        }
    }

    private fun appModules(): List<Module> = listOf(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        localModule,
        workManagerModule,
    )
}
