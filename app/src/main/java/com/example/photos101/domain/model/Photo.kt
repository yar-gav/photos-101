package com.example.photos101.domain.model

/**
 * Domain model for a photo in list/grid views (recent or search results).
 */
data class Photo(
    val id: String,
    val title: String,
    val ownerName: String,
    val dateTaken: String?,
    val thumbnailUrl: String?,
    val secret: String,
    val server: String,
)
