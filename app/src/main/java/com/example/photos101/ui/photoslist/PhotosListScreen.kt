package com.example.photos101.ui.photoslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.photos101.R
import com.example.photos101.domain.model.Photo
import com.example.photos101.ui.theme.Photos101Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosListScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotosListViewModel? = null,
    previewState: PhotosListState? = null,
    previewSearchInput: String = "",
) {
    val fromViewModel = viewModel != null
    val state by if (fromViewModel) {
        viewModel.state.collectAsStateWithLifecycle()
    } else {
        remember(previewState) { mutableStateOf(previewState ?: PhotosListState.Loading()) }
    }
    val searchInput by if (fromViewModel) {
        viewModel.searchInput.collectAsStateWithLifecycle()
    } else {
        remember(previewSearchInput) { mutableStateOf(previewSearchInput) }
    }
    var userExpandedSearch by remember { mutableStateOf(false) }
    val isSearchExpanded = searchInput.isNotBlank() || userExpandedSearch
    val searchFocusRequester = remember { FocusRequester() }

    val dispatch: (PhotosListUiActions) -> Unit = remember(viewModel) {
        if (fromViewModel) {
            { viewModel.dispatch(it) }
        } else {
            {}
        }
    }

    LaunchedEffect(isSearchExpanded && searchInput.isEmpty()) {
        if (isSearchExpanded) {
            searchFocusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PhotosListTopBar(
                searchInput = searchInput,
                isSearchExpanded = isSearchExpanded,
                onQueryChange = { dispatch(PhotosListUiActions.QueryChanged(it)) },
                onClearSearch = {
                    userExpandedSearch = false
                    dispatch(PhotosListUiActions.ClearSearch)
                },
                onExpandSearch = { userExpandedSearch = true },
                searchFocusRequester = searchFocusRequester,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 8.dp),
        ) {
            PhotosListContent(
                state = state,
                onRetry = { dispatch(PhotosListUiActions.Retry) },
                onPhotoClick = { photo -> dispatch(PhotosListUiActions.OpenPhoto(photo.id, photo.secret)) },
                onLoadMore = { dispatch(PhotosListUiActions.LoadNextPage) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotosListTopBar(
    searchInput: String,
    isSearchExpanded: Boolean,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onExpandSearch: () -> Unit,
    searchFocusRequester: FocusRequester,
) {
    TopAppBar(
        title = {
            if (isSearchExpanded) {
                TextField(
                    value = searchInput,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    singleLine = true,
                )
            } else {
                Text(stringResource(R.string.photos_title))
            }
        },
        actions = {
            if (isSearchExpanded) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_search_content_description),
                    )
                }
            } else {
                IconButton(onClick = onExpandSearch) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_content_description),
                    )
                }
            }
        },
    )
}

@Composable
private fun PhotosListContent(
    state: PhotosListState,
    onRetry: () -> Unit,
    onPhotoClick: (Photo) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is PhotosListState.Loading -> PhotosListLoading(modifier = modifier)
        is PhotosListState.Error -> PhotosListError(
            message = state.throwable.message ?: stringResource(R.string.unknown_error),
            onRetry = onRetry,
            modifier = modifier,
        )
        is PhotosListState.Empty -> PhotosListEmpty(modifier = modifier)
        is PhotosListState.Photos -> PhotosGrid(
            photos = state.items,
            isLoadingMore = state.isLoadingMore,
            hasMore = state.hasMore,
            onPhotoClick = onPhotoClick,
            onLoadMore = onLoadMore,
            modifier = modifier,
        )
    }
}

@Composable
private fun PhotosListLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PhotosListError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.error_message, message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun PhotosListEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_photos),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// --- Previews ---

@Preview(name = "Loading")
@Composable
private fun PhotosListScreenPreviewLoading() {
    Photos101Theme {
        PhotosListScreen(
            viewModel = null,
            previewState = PhotosListState.Loading(),
            previewSearchInput = "",
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun PhotosListScreenPreviewEmpty() {
    Photos101Theme {
        PhotosListScreen(
            viewModel = null,
            previewState = PhotosListState.Empty(),
            previewSearchInput = "",
        )
    }
}

@Preview(name = "Error")
@Composable
private fun PhotosListScreenPreviewError() {
    Photos101Theme {
        PhotosListScreen(
            viewModel = null,
            previewState = PhotosListState.Error(Throwable("Network error"), query = null),
            previewSearchInput = "",
        )
    }
}

private val previewSamplePhotos = listOf(
    Photo("1", "Photo 1", "owner1", null, null, "s1", "srv1"),
    Photo("2", "Photo 2", "owner2", null, null, "s2", "srv2"),
    Photo("3", "Photo 3", "owner3", null, null, "s3", "srv3"),
)

@Preview(name = "Recent")
@Composable
private fun PhotosListScreenPreviewRecent() {
    Photos101Theme {
        PhotosListScreen(
            viewModel = null,
            previewState = PhotosListState.Photos(
                query = null,
                items = previewSamplePhotos,
                currentPage = 1,
                totalPages = 2,
                isLoadingMore = false,
            ),
            previewSearchInput = "",
        )
    }
}

@Preview(name = "Search results")
@Composable
private fun PhotosListScreenPreviewSearchResults() {
    Photos101Theme {
        PhotosListScreen(
            viewModel = null,
            previewState = PhotosListState.Photos(
                query = "cats",
                items = previewSamplePhotos,
                currentPage = 1,
                totalPages = 1,
                isLoadingMore = false,
            ),
            previewSearchInput = "cats",
        )
    }
}
