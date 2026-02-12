package com.example.photos101.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Flickr API response wrapper for list methods (getRecent, search).
 * With nojsoncallback=1 the root is this object (no callback wrapper).
 */
@Serializable
data class PhotosResponseDto(
    @SerialName("stat") val stat: String = "ok",
    @SerialName("photos") val photos: PhotosWrapperDto? = null,
    @SerialName("code") val code: Int? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class PhotosWrapperDto(
    @SerialName("page") val page: Int = 1,
    @SerialName("pages") val pages: Int = 1,
    @SerialName("perpage") val perpage: Int = 0,
    @SerialName("total") val totalRaw: String = "0", // Flickr may return total as string
    @SerialName("photo") val photo: List<PhotoDto> = emptyList(),
)

@Serializable
data class PhotoDto(
    @SerialName("id") val id: String,
    @SerialName("owner") val owner: String = "",
    @SerialName("secret") val secret: String = "",
    @SerialName("server") val server: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("url_s") val urlS: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("date_taken") val dateTaken: String? = null,
)
