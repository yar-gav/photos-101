package com.example.photos101.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Flickr API response wrapper for list methods (getRecent, search).
 * With nojsoncallback=1 the root is this object (no callback wrapper).
 */
@Serializable
data class PhotosResponseDto(
    val stat: String = "ok",
    val photos: PhotosWrapperDto? = null,
    val code: Int? = null,
    val message: String? = null,
)

@Serializable
data class PhotosWrapperDto(
    val page: Int = 1,
    val pages: Int = 1,
    val perpage: Int = 0,
    @SerialName("total") val totalRaw: String = "0", // Flickr may return total as string
    val photo: List<PhotoDto> = emptyList(),
)

@Serializable
data class PhotoDto(
    val id: String,
    val owner: String = "",
    val secret: String = "",
    val server: String = "",
    val title: String = "",
    @SerialName("url_s") val urlS: String? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("date_taken") val dateTaken: String? = null,
)
