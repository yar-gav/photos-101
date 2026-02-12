package com.example.photos101.data.repository

import com.example.photos101.data.remote.FlickrApi
import com.example.photos101.data.remote.dto.PhotosResponseDto
import com.example.photos101.data.remote.mapper.toDetailDomain
import com.example.photos101.data.remote.mapper.toDomain
import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.model.PhotoDetail
import com.example.photos101.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PhotoRepositoryImpl(
    private val api: FlickrApi,
) : PhotoRepository {

    override suspend fun getRecentPhotos(page: Int, perPage: Int): Result<PagedResult<Photo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.getRecent(perPage = perPage, page = page)
                response.toResult() ?: response.toErrorResult()
            }.fold(
                onSuccess = { it },
                onFailure = { Result.failure(translateException(it)) }
            )
        }

    override suspend fun searchPhotos(query: String, page: Int, perPage: Int): Result<PagedResult<Photo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.search(text = query, perPage = perPage, page = page)
                response.toResult() ?: response.toErrorResult()
            }.fold(
                onSuccess = { it },
                onFailure = { Result.failure(translateException(it)) }
            )
        }

    override suspend fun getPhotoInfo(photoId: String, secret: String?): Result<PhotoDetail> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.getInfo(photoId = photoId, secret = secret)
                when {
                    response.stat == "ok" && response.photo != null ->
                        Result.success(response.photo.toDetailDomain())
                    else ->
                        Result.failure(
                            FlickrApiException(
                                code = response.code ?: -1,
                                message = response.message ?: "Unknown error"
                            )
                        )
                }
            }.fold(
                onSuccess = { it },
                onFailure = { Result.failure(translateException(it)) }
            )
        }

    private fun PhotosResponseDto.toResult(): Result<PagedResult<Photo>>? {
        if (stat != "ok" || photos == null) return null
        val wrapper = photos
        val items = wrapper.photo.map { it.toDomain() }
        val result = PagedResult(
            items = items,
            page = wrapper.page,
            totalPages = wrapper.pages,
            totalCount = wrapper.totalRaw
        )
        return Result.success(result)
    }

    private fun PhotosResponseDto.toErrorResult(): Result<PagedResult<Photo>> =
        Result.failure(
            FlickrApiException(
                code = code ?: -1,
                message = message ?: "Unknown error"
            )
        )

    private fun translateException(t: Throwable): Throwable = when (t) {
        is FlickrApiException -> t
        is IOException -> IOException("Network error: ${t.message}", t)
        else -> t
    }
}

class FlickrApiException(
    val code: Int,
    override val message: String,
) : Exception(message)
