package com.example.photos101.ui.photodetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.photos101.R
import com.example.photos101.domain.model.PhotoDetail
import com.example.photos101.ui.theme.Photos101Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    onBack: () -> Unit,
    viewModel: PhotoDetailViewModel? = null,
    previewState: PhotoDetailState? = null,
    modifier: Modifier = Modifier,
) {
    if (viewModel != null) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        PhotoDetailScaffold(
            state = state,
            onBack = onBack,
            onRetry = { viewModel.loadDetail() },
            modifier = modifier,
        )
    } else {
        val state = previewState ?: PhotoDetailState.Loading
        PhotoDetailScaffold(
            state = state,
            onBack = onBack,
            onRetry = {},
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailScaffold(
    state: PhotoDetailState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PhotoDetailTopBar(onBack = onBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PhotoDetailContentByState(state = state, onRetry = onRetry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_content_description),
                )
            }
        },
    )
}

@Composable
private fun PhotoDetailContentByState(
    state: PhotoDetailState,
    onRetry: () -> Unit,
) {
    when (val s = state) {
        is PhotoDetailState.Loading -> PhotoDetailLoading()
        is PhotoDetailState.Error -> PhotoDetailError(
            message = s.throwable.message ?: stringResource(R.string.unknown),
            onRetry = onRetry,
        )
        is PhotoDetailState.Success -> PhotoDetailContent(
            detail = s.detail,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PhotoDetailLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PhotoDetailError(
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_message, message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun PhotoDetailContent(
    detail: PhotoDetail,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = detail.largeImageUrl,
            contentDescription = detail.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(16.dp),
        ) {
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.photo_by_owner, detail.ownerName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            detail.dateTaken?.let { date ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.photo_taken_date, date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// --- Previews ---

@Preview(name = "Loading")
@Composable
private fun PhotoDetailScreenPreviewLoading() {
    Photos101Theme {
        PhotoDetailScreen(
            onBack = {},
            viewModel = null,
            previewState = PhotoDetailState.Loading,
        )
    }
}

@Preview(name = "Error")
@Composable
private fun PhotoDetailScreenPreviewError() {
    Photos101Theme {
        PhotoDetailScreen(
            onBack = {},
            viewModel = null,
            previewState = PhotoDetailState.Error(Throwable("Failed to load")),
        )
    }
}

private val previewDetail = PhotoDetail(
    id = "1",
    title = "Sample Photo",
    ownerName = "photographer",
    dateTaken = "2024-01-15",
    largeImageUrl = null,
)

@Preview(name = "Success")
@Composable
private fun PhotoDetailScreenPreviewSuccess() {
    Photos101Theme {
        PhotoDetailScreen(
            onBack = {},
            viewModel = null,
            previewState = PhotoDetailState.Success(previewDetail),
        )
    }
}
