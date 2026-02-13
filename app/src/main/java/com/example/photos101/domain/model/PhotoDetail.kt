package com.example.photos101.domain.model

/**
 * Domain model for full photo details (detail screen).
 */
data class PhotoDetail(
    val id: String,
    val title: String,
    val ownerName: String,
    val dateTaken: String?,
    val largeImageUrl: String?,
)
