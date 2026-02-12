package com.example.photos101.domain.usecase

import com.example.photos101.domain.model.PhotoDetail
import com.example.photos101.domain.repository.PhotoRepository

/**
 * Fetches full details for a single photo (for the detail screen).
 */
class GetPhotoInfoUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(photoId: String, secret: String? = null): Result<PhotoDetail> =
        repository.getPhotoInfo(photoId = photoId, secret = secret)
}
