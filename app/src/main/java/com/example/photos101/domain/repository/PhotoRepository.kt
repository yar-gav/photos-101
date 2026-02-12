package com.example.photos101.domain.repository

import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.model.PhotoDetail

/**
 * Repository for fetching photos from Flickr (recent, search, detail).
 */
interface PhotoRepository {

    /**
     * Fetches a page of the most recently uploaded public photos.
     */
    suspend fun getRecentPhotos(page: Int = 1, perPage: Int = 30): Result<PagedResult<Photo>>

    /**
     * Searches public photos by text (title, description, tags).
     */
    suspend fun searchPhotos(query: String, page: Int = 1, perPage: Int = 30): Result<PagedResult<Photo>>

    /**
     * Fetches full details for a single photo (for detail screen).
     * [secret] is optional but recommended when available for correct permission handling.
     */
    suspend fun getPhotoInfo(photoId: String, secret: String? = null): Result<PhotoDetail>
}
