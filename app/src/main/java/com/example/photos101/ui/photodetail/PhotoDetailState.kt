package com.example.photos101.ui.photodetail

import com.example.photos101.domain.model.PhotoDetail

/**
 * State for the photo detail screen.
 */
sealed class PhotoDetailState {
    data object Loading : PhotoDetailState()
    data class Success(val detail: PhotoDetail) : PhotoDetailState()
    data class Error(val throwable: Throwable) : PhotoDetailState()
}
