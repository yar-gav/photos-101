package com.example.photos101.ui.photoslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PhotosListScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotosListViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { _ ->
            // Navigation will be handled when NavHost is added
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 8.dp),
    ) {
        when (val s = state) {
            is PhotosListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is PhotosListState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorContent(
                        message = s.throwable.message ?: "Unknown error",
                        onRetry = { viewModel.dispatch(PhotosListUiActions.Retry) },
                    )
                }
            }
            is PhotosListState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No photos",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            is PhotosListState.RecentPhotos -> {
                PhotosGrid(
                    photos = s.items,
                    isLoadingMore = s.isLoadingMore,
                    hasMore = s.hasMore,
                    onPhotoClick = { photo ->
                        viewModel.dispatch(PhotosListUiActions.OpenPhoto(photo.id, photo.secret))
                    },
                    onLoadMore = { viewModel.dispatch(PhotosListUiActions.LoadNextPage) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is PhotosListState.SearchResults -> {
                PhotosGrid(
                    photos = s.items,
                    isLoadingMore = s.isLoadingMore,
                    hasMore = s.hasMore,
                    onPhotoClick = { photo ->
                        viewModel.dispatch(PhotosListUiActions.OpenPhoto(photo.id, photo.secret))
                    },
                    onLoadMore = { viewModel.dispatch(PhotosListUiActions.LoadNextPage) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
