package com.example.photos101.ui.photoslist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            is PhotosListState.Loading -> CircularProgressIndicator()
            is PhotosListState.Error -> Text(
                text = "Error: ${s.throwable.message ?: "Unknown"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            is PhotosListState.Empty -> Text(
                text = "No photos",
                style = MaterialTheme.typography.bodyLarge,
            )
            is PhotosListState.RecentPhotos -> Text(
                text = "Recent photos: ${s.items.size} loaded",
                style = MaterialTheme.typography.bodyLarge,
            )
            is PhotosListState.SearchResults -> Text(
                text = "Search results: ${s.items.size} loaded",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
