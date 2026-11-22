package com.samtv.app.ui.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.samtv.app.data.MediaRepository
import com.samtv.app.data.MovieItem
import com.samtv.app.data.MovieSort
import com.samtv.app.ui.common.ErrorBox
import com.samtv.app.ui.common.LoadingBox
import com.samtv.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieGridScreen(
    yearTitle: String,
    yearUrl: String,
    onMovieClick: (MovieItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember(yearUrl) { mutableStateOf<UiState<List<MovieItem>>>(UiState.Loading) }
    var sort by remember(yearUrl) { mutableStateOf(MovieSort.NEWEST) }

    LaunchedEffect(yearUrl) {
        state = UiState.Loading
        state = withContext(Dispatchers.IO) {
            runCatching { MediaRepository.listMovies(yearUrl) }
                .fold(
                    onSuccess = { UiState.Ready(it) },
                    onFailure = { UiState.Error(it.message ?: "Failed to load movies") },
                )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = yearTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Movies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            SortChip(
                sort = sort,
                onClick = {
                    sort = if (sort == MovieSort.NEWEST) MovieSort.NAME else MovieSort.NEWEST
                },
            )
        }

        when (val current = state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Ready -> {
                if (current.data.isEmpty()) {
                    ErrorBox("No movies found")
                } else {
                    val movies = remember(current.data, sort) {
                        MediaRepository.sortedMovies(current.data, sort)
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(movies, key = { it.folderUrl }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SortChip(
    sort: MovieSort,
    onClick: () -> Unit,
) {
    val label = when (sort) {
        MovieSort.NEWEST -> "Sort: Newest"
        MovieSort.NAME -> "Sort: Name"
    }
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            pressedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MovieCard(
    movie: MovieItem,
    onClick: () -> Unit,
) {
    var posterUrl by remember(movie.folderUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(movie.folderUrl) {
        posterUrl = withContext(Dispatchers.IO) {
            MediaRepository.posterUrl(movie.folderUrl)
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
            pressedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                if (posterUrl != null) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = "…",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }
            Text(
                text = movie.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }
    }
}
