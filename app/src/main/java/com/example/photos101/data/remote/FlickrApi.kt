package com.example.photos101.data.remote

import com.example.photos101.data.remote.dto.PhotoInfoResponseDto
import com.example.photos101.data.remote.dto.PhotosResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Flickr REST API.
 * Base URL: https://api.flickr.com/services/rest/
 * api_key, format=json, nojsoncallback=1 are added by [FlickrApiKeyInterceptor].
 */
interface FlickrApi {

    @GET(".")
    suspend fun getRecent(
        @Query("method") method: String = "flickr.photos.getRecent",
        @Query("extras") extras: String = EXTRAS,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
    ): PhotosResponseDto

    @GET(".")
    suspend fun search(
        @Query("method") method: String = "flickr.photos.search",
        @Query("text") text: String,
        @Query("extras") extras: String = EXTRAS,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
    ): PhotosResponseDto

    @GET(".")
    suspend fun getInfo(
        @Query("method") method: String = "flickr.photos.getInfo",
        @Query("photo_id") photoId: String,
        @Query("secret") secret: String? = null,
    ): PhotoInfoResponseDto

    companion object {
        const val EXTRAS = "url_s,owner_name,date_taken"
    }
}
