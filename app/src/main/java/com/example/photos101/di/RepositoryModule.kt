package com.example.photos101.di

import com.example.photos101.data.repository.PhotoRepositoryImpl
import com.example.photos101.domain.repository.PhotoRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }
}
