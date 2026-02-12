package com.example.photos101.ui.photoslist

/**
 * UI actions for the photos list screen.
 */
sealed class PhotosListUiActions {
    /** Load initial content: recent photos when no query, or search when query is set. */
    data object LoadInitial : PhotosListUiActions()

    /** User changed the search query (e.g. typed in the search field). */
    data class QueryChanged(val query: String) : PhotosListUiActions()

    /** User submitted search (e.g. pressed search or enter). */
    data class Search(val query: String) : PhotosListUiActions()

    /** Load the next page (infinite scroll). */
    data object LoadNextPage : PhotosListUiActions()

    /** User tapped a photo to open detail. */
    data class OpenPhoto(val photoId: String, val secret: String) : PhotosListUiActions()

    /** User tapped retry after an error. */
    data object Retry : PhotosListUiActions()

    /** User requested refresh (pull-to-refresh or similar). */
    data object Refresh : PhotosListUiActions()
}
