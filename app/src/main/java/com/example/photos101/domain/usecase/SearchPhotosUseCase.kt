package com.example.photos101.domain.usecase

import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.repository.PhotoRepository

/**
 * Searches public photos by text (title, description, tags).
 */
class SearchPhotosUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(query: String, page: Int = 1, perPage: Int = 30): Result<PagedResult<Photo>> =
        repository.searchPhotos(query = query, page = page, perPage = perPage)
}
