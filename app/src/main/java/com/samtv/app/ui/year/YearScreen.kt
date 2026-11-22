package com.samtv.app.ui.year

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.samtv.app.data.DirEntry
import com.samtv.app.data.MediaRepository
import com.samtv.app.ui.common.ErrorBox
import com.samtv.app.ui.common.LoadingBox
import com.samtv.app.ui.common.ScreenHeader
import com.samtv.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun YearScreen(
    categoryTitle: String,
    serverId: String,
    categoryPath: String,
    onYearClick: (DirEntry) -> Unit,
    onFlatCategory: (categoryUrl: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember(serverId, categoryPath) {
        mutableStateOf<UiState<List<DirEntry>>>(UiState.Loading)
    }

    LaunchedEffect(serverId, categoryPath) {
        state = UiState.Loading
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val url = MediaRepository.categoryUrl(serverId, categoryPath)
                val entries = MediaRepository.listYears(serverId, categoryPath)
                Triple(url, entries, MediaRepository.looksLikeYearList(entries))
            }
        }

        result.fold(
            onSuccess = { (url, entries, isYearList) ->
                if (!isYearList) {
                    onFlatCategory(url)
                } else {
                    state = UiState.Ready(entries)
                }
            },
            onFailure = {
                state = UiState.Error(it.message ?: "Failed to load years")
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 28.dp),
    ) {
        ScreenHeader(title = categoryTitle, subtitle = "Select year")

        when (val current = state) {
            UiState.Loading -> LoadingBox()
            is UiState.Error -> ErrorBox(current.message)
            is UiState.Ready -> {
                if (current.data.isEmpty()) {
                    ErrorBox("No folders found")
                } else {
                    TvLazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(current.data, key = { it.url }) { year ->
                            YearRow(
                                title = year.name,
                                onClick = { onYearClick(year) },
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
private fun YearRow(
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            pressedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        )
    }
}
