package com.example.photos101.di

import com.example.photos101.ui.photodetail.PhotoDetailViewModel
import com.example.photos101.ui.photoslist.PhotosListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { PhotosListViewModel(get(), get(), get(), get()) }
    viewModel { (photoId: String, secret: String?) ->
        PhotoDetailViewModel(get(), photoId, secret)
    }
}
