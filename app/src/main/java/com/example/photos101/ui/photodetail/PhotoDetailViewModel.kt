package com.example.photos101.ui.photodetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photos101.domain.usecase.GetPhotoInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the photo detail screen. Loads full photo info and exposes it as state.
 */
class PhotoDetailViewModel(
    private val getPhotoInfoUseCase: GetPhotoInfoUseCase,
    private val photoId: String,
    private val secret: String?,
) : ViewModel() {

    private val _state = MutableStateFlow<PhotoDetailState>(PhotoDetailState.Loading)
    val state: StateFlow<PhotoDetailState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (photoId.isBlank()) {
            _state.value = PhotoDetailState.Error(IllegalArgumentException("Missing photo ID"))
            return
        }
        viewModelScope.launch {
            _state.value = PhotoDetailState.Loading
            getPhotoInfoUseCase(photoId = photoId, secret = secret)
                .onSuccess { detail ->
                    _state.value = PhotoDetailState.Success(detail)
                }
                .onFailure { t ->
                    _state.value = PhotoDetailState.Error(t)
                }
        }
    }
}
