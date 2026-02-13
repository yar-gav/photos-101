package com.example.photos101.ui.photoslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.photos101.domain.model.Photo
import com.example.photos101.domain.model.PagedResult
import com.example.photos101.worker.PhotosPollWorker
import com.example.photos101.data.local.ActiveSearchPollStateDataSource
import com.example.photos101.domain.usecase.GetRecentPhotosUseCase
import com.example.photos101.domain.usecase.SearchPhotosUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * MVI ViewModel for the photos list screen (recent + search with paging).
 * Uses a single "photos list" state: [PhotosListState.Photos] with [query] null for recent, non-null for search.
 * Schedules background polling (15 min) only when there is an active search; cancels when search is cleared.
 */
class PhotosListViewModel(
    private val getRecentPhotosUseCase: GetRecentPhotosUseCase,
    private val searchPhotosUseCase: SearchPhotosUseCase,
    private val activeSearchPollStateDataSource: ActiveSearchPollStateDataSource,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _state = MutableStateFlow<PhotosListState>(PhotosListState.Loading(null))
    val state: StateFlow<PhotosListState> = _state.asStateFlow()

    private val _searchInput = MutableStateFlow("")
    val searchInput: StateFlow<String> = _searchInput.asStateFlow()

    private val _events = MutableSharedFlow<PhotosListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhotosListEvent> = _events.asSharedFlow()

    private var searchDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            restoreSavedSearchAndLoadInitial()
        }
        startPollStateStaleCheck()
    }

    fun dispatch(intent: PhotosListUiActions) {
        viewModelScope.launch {
            when (intent) {
                is PhotosListUiActions.LoadInitial -> loadInitial()
                is PhotosListUiActions.QueryChanged -> {
                    _searchInput.value = intent.query
                    debouncedSearch(intent.query)
                }
                is PhotosListUiActions.LoadNextPage -> loadNextPage()
                is PhotosListUiActions.OpenPhoto ->
                    _events.emit(PhotosListEvent.NavigateToDetail(intent.photoId, intent.secret))
                is PhotosListUiActions.Retry -> loadInitial()
                is PhotosListUiActions.ClearSearch -> clearSearchAndLoadRecent()
            }
        }
    }

    // --- Initialization & polling ---

    private suspend fun restoreSavedSearchAndLoadInitial() {
        val saved = activeSearchPollStateDataSource.getActiveSearchPollState()
        if (!saved?.activeSearchQuery.isNullOrBlank()) {
            _searchInput.value = saved.activeSearchQuery
        }
        loadInitial()
    }

    private fun startPollStateStaleCheck() {
        activeSearchPollStateDataSource.activeSearchPollState
            .distinctUntilChanged()
            .onEach { pollState ->
                if (pollState == null || pollState.activeSearchQuery.isBlank()) return@onEach
                val current = _state.value
                if (current is PhotosListState.Photos && current.query == pollState.activeSearchQuery) {
                    val ourFirstPageIds = current.items.take(PAGE_SIZE).map { it.id }
                    if (ourFirstPageIds != pollState.firstPagePhotoIds) {
                        loadInitial()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun cancelPolling() {
        workManager.cancelUniqueWork(WORK_NAME)
        viewModelScope.launch {
            activeSearchPollStateDataSource.clearPollState()
        }
    }

    private fun clearSearchAndLoadRecent() {
        searchDebounceJob?.cancel()
        cancelPolling()
        _searchInput.value = ""
        viewModelScope.launch { loadPage(1, query = null, replace = true) }
    }

    // --- Load triggers ---

    private suspend fun loadInitial() {
        val query = _searchInput.value.ifBlank { null }
        loadPage(1, query, replace = true)
    }

    private suspend fun loadNextPage() {
        val current = _state.value as? PhotosListState.Photos ?: return
        if (!current.isLoadingMore && current.hasMore) {
            loadPage(current.currentPage + 1, current.query, replace = false)
        }
    }

    private fun debouncedSearch(query: String) {
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            loadPage(1, query.ifBlank { null }, replace = true)
        }
    }

    // --- Single load path (recent = query null, search = query non-null) ---

    private suspend fun loadPage(page: Int, query: String?, replace: Boolean) {
        setLoadingState(query, replace)
        val result = if (query.isNullOrBlank()) {
            getRecentPhotosUseCase(page = page, perPage = PAGE_SIZE)
        } else {
            searchPhotosUseCase(query = query, page = page, perPage = PAGE_SIZE)
        }
        result
            .onSuccess { applySuccess(it, query, replace) }
            .onFailure { applyFailure(it, query, replace) }
    }

    private fun setLoadingState(query: String?, replace: Boolean) {
        if (replace) {
            _state.value = PhotosListState.Loading(query)
        } else {
            val current = _state.value as? PhotosListState.Photos
            if (current is PhotosListState.Photos && current.query == query) {
                _state.value = current.copy(isLoadingMore = true)
            }
        }
    }

    private fun applySuccess(result: PagedResult<Photo>, query: String?, replace: Boolean) {
        val current = _state.value as? PhotosListState.Photos
        val isAppend = !replace && current != null && current.query == query && current.isLoadingMore
        val newItems = if (isAppend) {
            (current.items + result.items).distinctBy { it.id }
        } else {
            result.items
        }
        _state.value = stateFromItems(newItems, result.page, result.totalPages, query)
        if (!query.isNullOrEmpty() && !isAppend) {
            val newState = _state.value as? PhotosListState.Photos
            if (newState != null) {
                persistAndSchedulePolling(
                    newState.items.take(PAGE_SIZE).map { it.id },
                    query,
                )
            }
        }
    }

    private fun applyFailure(throwable: Throwable, query: String?, replace: Boolean) {
        val current = _state.value as? PhotosListState.Photos
        _state.value = when {
            replace || current == null || current.query != query ->
                PhotosListState.Error(throwable, query)
            else ->
                current.copy(isLoadingMore = false)
        }
    }

    private fun stateFromItems(
        items: List<Photo>,
        page: Int,
        totalPages: Int,
        query: String?,
    ): PhotosListState =
        if (items.isEmpty()) PhotosListState.Empty(query)
        else PhotosListState.Photos(
            query = query,
            items = items,
            currentPage = page,
            totalPages = totalPages,
            isLoadingMore = false,
        )

    private fun persistAndSchedulePolling(firstPageIds: List<String>, query: String) {
        viewModelScope.launch {
            activeSearchPollStateDataSource.savePollState(query, firstPageIds)
            val request = PeriodicWorkRequestBuilder<PhotosPollWorker>(
                POLL_INTERVAL_MINUTES, TimeUnit.MINUTES
            ).setInitialDelay(POLL_INTERVAL_MINUTES, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request,
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
        private const val DEBOUNCE_MS = 400L
        private const val POLL_INTERVAL_MINUTES = 15L
        const val WORK_NAME = "photos_poll"
    }
}

/**
 * One-time events (e.g. navigation) for the photos list screen.
 */
sealed class PhotosListEvent {
    data class NavigateToDetail(val photoId: String, val secret: String) : PhotosListEvent()
}
