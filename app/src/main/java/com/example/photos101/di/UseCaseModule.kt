package com.example.photos101.di

import com.example.photos101.domain.usecase.GetPhotoInfoUseCase
import com.example.photos101.domain.usecase.GetRecentPhotosUseCase
import com.example.photos101.domain.usecase.SearchPhotosUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetRecentPhotosUseCase(get()) }
    factory { SearchPhotosUseCase(get()) }
    factory { GetPhotoInfoUseCase(get()) }
}
