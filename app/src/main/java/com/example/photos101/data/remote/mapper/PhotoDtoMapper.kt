package com.example.photos101.data.remote.mapper

import com.example.photos101.data.remote.dto.PhotoDto
import com.example.photos101.data.remote.dto.PhotoInfoDto
import com.example.photos101.domain.model.PagedResult
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.model.PhotoDetail

fun PhotoDto.toDomain(): Photo = Photo(
    id = id,
    title = title.ifBlank { "Untitled" },
    ownerName = ownerName ?: owner.ifBlank { "Unknown" },
    dateTaken = dateTaken,
    thumbnailUrl = urlS,
    secret = secret,
    server = server,
)

fun PhotoInfoDto.toDetailDomain(): PhotoDetail {
    val titleText = title?.content?.takeIf { it.isNotBlank() } ?: "Untitled"
    val ownerDisplay = owner?.username?.takeIf { it.isNotBlank() } ?: owner?.nsid ?: "Unknown"
    // Build larger image URL: https://live.staticflickr.com/{server}/{id}_{secret}_z.jpg
    val largeUrl = "https://live.staticflickr.com/$server/${id}_${secret}_z.jpg"
    return PhotoDetail(
        id = id,
        title = titleText,
        ownerName = ownerDisplay,
        dateTaken = dates?.taken,
        largeImageUrl = largeUrl,
        description = description?.content?.takeIf { it.isNotBlank() },
    )
}

/** Parse Flickr's total (may be string) to Int. */
fun parseTotal(totalRaw: String): Int = totalRaw.toIntOrNull() ?: 0
