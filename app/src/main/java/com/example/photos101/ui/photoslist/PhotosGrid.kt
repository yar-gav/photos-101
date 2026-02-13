package com.example.photos101.ui.photoslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.photos101.R
import coil.compose.AsyncImage
import com.example.photos101.domain.model.Photo
import com.example.photos101.ui.theme.Photos101Theme

private const val GRID_COLUMNS = 3
private const val LOAD_MORE_THRESHOLD = 5

/**
 * Builds thumbnail URL from Flickr photo ids when [Photo.thumbnailUrl] is null.
 */
fun Photo.thumbnailUrlOrBuilt(): String =
    thumbnailUrl ?: "https://live.staticflickr.com/$server/${id}_${secret}_s.jpg"

@Composable
fun PhotosGrid(
    photos: List<Photo>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onPhotoClick: (Photo) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(GRID_COLUMNS),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = photos,
                key = { it.id },
            ) { photo ->
                PhotoGridItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp),
                )
            }
            if (isLoadingMore) {
                item(span = { GridItemSpan(GRID_COLUMNS) }) {
                    GridLoadingFooter()
                }
            }
        }

        // Endless scroll: when user scrolls near the end, fetch next page
        LaunchedEffect(gridState, hasMore, isLoadingMore) {
            snapshotFlow { gridState.layoutInfo }
                .collect { layoutInfo ->
                    if (!hasMore || isLoadingMore) return@collect
                    val totalItems = layoutInfo.totalItemsCount
                    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    if (totalItems > 0 && lastVisibleIndex >= totalItems - LOAD_MORE_THRESHOLD) {
                        onLoadMore()
                    }
                }
        }
    }
}

@Composable
private fun GridLoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = photo.thumbnailUrlOrBuilt(),
            contentDescription = photo.title.ifBlank { stringResource(R.string.photo_content_description, photo.id) },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private val gridPreviewPhotos = listOf(
    Photo("1", "Photo 1", "u1", null, null, "s1", "srv1"),
    Photo("2", "Photo 2", "u2", null, null, "s2", "srv2"),
    Photo("3", "Photo 3", "u3", null, null, "s3", "srv3"),
)

@Preview(name = "Grid")
@Composable
private fun PhotosGridPreview() {
    Photos101Theme {
        PhotosGrid(
            photos = gridPreviewPhotos,
            isLoadingMore = false,
            hasMore = false,
            onPhotoClick = {},
            onLoadMore = {},
        )
    }
}

@Preview(name = "Grid loading more")
@Composable
private fun PhotosGridLoadingMorePreview() {
    Photos101Theme {
        PhotosGrid(
            photos = gridPreviewPhotos,
            isLoadingMore = true,
            hasMore = true,
            onPhotoClick = {},
            onLoadMore = {},
        )
    }
}
