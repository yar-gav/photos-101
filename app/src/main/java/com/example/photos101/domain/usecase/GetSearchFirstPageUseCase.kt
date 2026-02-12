package com.example.photos101.domain.usecase

import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.repository.PhotoRepository

/**
 * Fetches the first page of search results for a query.
 * Used by background polling to detect new results (compare first page to previously seen).
 */
class GetSearchFirstPageUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(query: String, perPage: Int = 30): Result<PagedResult<Photo>> =
        repository.searchPhotos(query = query, page = 1, perPage = perPage)
}
