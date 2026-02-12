package com.example.photos101.ui.photoslist

import com.example.photos101.domain.model.Photo

/**
 * MVI state for the photos list screen as a sealed class.
 */
sealed class PhotosListState {

    /** Displaying the recent photos list (with optional load-more in progress). */
    data class RecentPhotos(
        val items: List<Photo>,
        val currentPage: Int,
        val totalPages: Int,
        val isLoadingMore: Boolean = false,
    ) : PhotosListState() {
        val hasMore: Boolean get() = currentPage < totalPages
    }

    /** Displaying search results (with optional load-more in progress). */
    data class SearchResults(
        val query: String,
        val items: List<Photo>,
        val currentPage: Int,
        val totalPages: Int,
        val isLoadingMore: Boolean = false,
    ) : PhotosListState() {
        val hasMore: Boolean get() = currentPage < totalPages
    }

    /** Initial or refresh load in progress. [query] is null when loading recent, non-null when loading search. */
    data class Loading(val query: String? = null) : PhotosListState()

    /** Load failed. [query] is set when the failed request was a search. */
    data class Error(val throwable: Throwable, val query: String? = null) : PhotosListState()

    /** No photos to show (e.g. empty search results). [query] is set when empty is from search. */
    data class Empty(val query: String? = null) : PhotosListState()
}
