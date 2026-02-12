package com.example.photos101.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Flickr API response for flickr.photos.getInfo.
 */
@Serializable
data class PhotoInfoResponseDto(
    @SerialName("stat") val stat: String = "ok",
    @SerialName("photo") val photo: PhotoInfoDto? = null,
    @SerialName("code") val code: Int? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class PhotoInfoDto(
    @SerialName("id") val id: String,
    @SerialName("secret") val secret: String = "",
    @SerialName("server") val server: String = "",
    @SerialName("owner") val owner: PhotoOwnerDto? = null,
    @SerialName("title") val title: ContentWrapperDto? = null,
    @SerialName("description") val description: ContentWrapperDto? = null,
    @SerialName("dates") val dates: PhotoDatesDto? = null,
)

@Serializable
data class PhotoOwnerDto(
    @SerialName("nsid") val nsid: String = "",
    @SerialName("username") val username: String = "",
)

/** Text content in Flickr JSON uses "_content" key. */
@Serializable
data class ContentWrapperDto(
    @SerialName("_content") val content: String = "",
)

@Serializable
data class PhotoDatesDto(
    @SerialName("taken") val taken: String? = null,
)
