package com.example.photos101.di

import com.example.photos101.worker.PhotosPollWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val workManagerModule = module {
    workerOf(::PhotosPollWorker)
}
