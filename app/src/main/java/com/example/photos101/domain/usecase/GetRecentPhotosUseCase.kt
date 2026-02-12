package com.example.photos101.domain.usecase

import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.repository.PhotoRepository

/**
 * Fetches a page of the most recently uploaded public photos.
 */
class GetRecentPhotosUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(page: Int = 1, perPage: Int = 30): Result<PagedResult<Photo>> =
        repository.getRecentPhotos(page = page, perPage = perPage)
}
