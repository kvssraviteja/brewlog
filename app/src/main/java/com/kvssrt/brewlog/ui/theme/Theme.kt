package com.kvssrt.brewlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrewlogColors = lightColorScheme(
    primary = Color(0xFF4C6A4A),
    onPrimary = Color.White,
    secondary = Color(0xFF8A5A44),
    onSecondary = Color.White,
    tertiary = Color(0xFF2F6F73),
    background = Color(0xFFFFFBF7),
    surface = Color(0xFFFFFBF7),
    surfaceVariant = Color(0xFFE5DCD2),
    onSurface = Color(0xFF211A16),
    onSurfaceVariant = Color(0xFF62564F),
)

@Composable
fun BrewlogTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BrewlogColors,
        content = content,
    )
}
