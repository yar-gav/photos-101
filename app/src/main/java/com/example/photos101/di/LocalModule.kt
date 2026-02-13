package com.example.photos101.di

import androidx.work.WorkManager
import com.example.photos101.data.local.ActiveSearchPollStateDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { ActiveSearchPollStateDataSource(androidContext()) }
    single { WorkManager.getInstance(androidContext()) }
}
