package com.example.photos101.data.local

/**
 * Persisted state for background polling: active search (or recent) and last known first-page result.
 */
data class ActiveSearchPollState(
    /** Empty string = recent photos; non-empty = search query. */
    val activeSearchQuery: String,
    /** Photo IDs from the first page at last persist. */
    val firstPagePhotoIds: List<String>,
)
