package com.example.photos101.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Flickr API response for flickr.photos.getInfo.
 */
@Serializable
data class PhotoInfoResponseDto(
    val stat: String = "ok",
    val photo: PhotoInfoDto? = null,
    val code: Int? = null,
    val message: String? = null,
)

@Serializable
data class PhotoInfoDto(
    val id: String,
    val secret: String = "",
    val server: String = "",
    val owner: PhotoOwnerDto? = null,
    val title: ContentWrapperDto? = null,
    val description: ContentWrapperDto? = null,
    val dates: PhotoDatesDto? = null,
)

@Serializable
data class PhotoOwnerDto(
    val nsid: String = "",
    val username: String = "",
)

/** Text content in Flickr JSON uses "_content" key. */
@Serializable
data class ContentWrapperDto(
    @SerialName("_content") val content: String = "",
)

@Serializable
data class PhotoDatesDto(
    val taken: String? = null,
)
