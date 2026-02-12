package com.example.photos101.ui.photoslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photos101.domain.usecase.GetRecentPhotosUseCase
import com.example.photos101.domain.usecase.SearchPhotosUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the photos list screen (recent + search with paging).
 */
class PhotosListViewModel(
    private val getRecentPhotosUseCase: GetRecentPhotosUseCase,
    private val searchPhotosUseCase: SearchPhotosUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<PhotosListState>(PhotosListState.Loading(null))
    val state: StateFlow<PhotosListState> = _state.asStateFlow()

    private val _searchInput = MutableStateFlow("")
    val searchInput: StateFlow<String> = _searchInput.asStateFlow()

    private val _events = MutableSharedFlow<PhotosListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhotosListEvent> = _events.asSharedFlow()

    init {
        dispatch(PhotosListUiActions.LoadInitial)
    }

    fun dispatch(intent: PhotosListUiActions) {
        viewModelScope.launch {
            when (intent) {
                is PhotosListUiActions.LoadInitial -> loadInitial()
                is PhotosListUiActions.QueryChanged -> _searchInput.value = intent.query
                is PhotosListUiActions.Search -> {
                    _searchInput.value = intent.query
                    loadSearchPage(1, intent.query, replace = true)
                }
                is PhotosListUiActions.LoadNextPage -> loadNextPage()
                is PhotosListUiActions.OpenPhoto -> _events.emit(
                    PhotosListEvent.NavigateToDetail(intent.photoId, intent.secret)
                )
                is PhotosListUiActions.Retry -> loadInitial()
                is PhotosListUiActions.Refresh -> loadInitial()
            }
        }
    }

    private suspend fun loadInitial() {
        val query = _searchInput.value
        if (query.isBlank()) {
            loadRecentPage(1, replace = true)
        } else {
            loadSearchPage(1, query, replace = true)
        }
    }

    private suspend fun loadNextPage() {
        when (val s = _state.value) {
            is PhotosListState.RecentPhotos ->
                if (!s.isLoadingMore && s.hasMore) loadRecentPage(s.currentPage + 1, replace = false)
            is PhotosListState.SearchResults ->
                if (!s.isLoadingMore && s.hasMore) loadSearchPage(s.currentPage + 1, s.query, replace = false)
            else -> { /* no-op */ }
        }
    }

    private suspend fun loadRecentPage(page: Int, replace: Boolean) {
        if (replace) _state.value = PhotosListState.Loading(null)
        else {
            val current = _state.value as? PhotosListState.RecentPhotos
            if (current != null) _state.value = current.copy(isLoadingMore = true)
        }
        getRecentPhotosUseCase(page = page, perPage = PAGE_SIZE)
            .onSuccess { result ->
                val current = _state.value as? PhotosListState.RecentPhotos
                val newItems = if (replace) result.items else (current?.items.orEmpty() + result.items)
                _state.value = when {
                    newItems.isEmpty() -> PhotosListState.Empty(null)
                    else -> PhotosListState.RecentPhotos(
                        items = newItems,
                        currentPage = result.page,
                        totalPages = result.totalPages,
                        isLoadingMore = false,
                    )
                }
            }
            .onFailure { t ->
                val current = _state.value as? PhotosListState.RecentPhotos
                if (replace || current == null) {
                    _state.value = PhotosListState.Error(t, null)
                } else {
                    // Keep list, clear loading-more flag so user can retry scroll
                    _state.value = current.copy(isLoadingMore = false)
                }
            }
    }

    private suspend fun loadSearchPage(page: Int, query: String, replace: Boolean) {
        if (query.isBlank()) return
        if (replace) _state.value = PhotosListState.Loading(query)
        else {
            val current = _state.value as? PhotosListState.SearchResults
            if (current != null && current.query == query) {
                _state.value = current.copy(isLoadingMore = true)
            }
        }
        searchPhotosUseCase(query = query, page = page, perPage = PAGE_SIZE)
            .onSuccess { result ->
                when (val current = _state.value) {
                    is PhotosListState.SearchResults -> if (!replace && current.query == query && current.isLoadingMore) {
                        _state.value = current.copy(
                            items = current.items + result.items,
                            currentPage = result.page,
                            totalPages = result.totalPages,
                            isLoadingMore = false,
                        )
                    } else {
                        _state.value = when {
                            result.items.isEmpty() -> PhotosListState.Empty(query)
                            else -> PhotosListState.SearchResults(
                                query = query,
                                items = result.items,
                                currentPage = result.page,
                                totalPages = result.totalPages,
                                isLoadingMore = false,
                            )
                        }
                    }
                    else -> _state.value = when {
                        result.items.isEmpty() -> PhotosListState.Empty(query)
                        else -> PhotosListState.SearchResults(
                            query = query,
                            items = result.items,
                            currentPage = result.page,
                            totalPages = result.totalPages,
                            isLoadingMore = false,
                        )
                    }
                }
            }
            .onFailure { t ->
                val current = _state.value as? PhotosListState.SearchResults
                if (replace || current == null || current.query != query) {
                    _state.value = PhotosListState.Error(t, query)
                } else {
                    _state.value = current.copy(isLoadingMore = false)
                }
            }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}

/**
 * One-time events (e.g. navigation) for the photos list screen.
 */
sealed class PhotosListEvent {
    data class NavigateToDetail(val photoId: String, val secret: String) : PhotosListEvent()
}
