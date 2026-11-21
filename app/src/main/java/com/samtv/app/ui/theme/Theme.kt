package com.samtv.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val SamDark = darkColorScheme(
    primary = Color(0xFFE8EEF7),
    onPrimary = Color(0xFF0B0F14),
    secondary = Color(0xFF7EB6FF),
    onSecondary = Color(0xFF0B0F14),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE8EEF7),
    surface = Color(0xFF151B24),
    onSurface = Color(0xFFE8EEF7),
    border = Color(0xFF2A3444),
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SamTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SamDark,
        content = content,
    )
}
