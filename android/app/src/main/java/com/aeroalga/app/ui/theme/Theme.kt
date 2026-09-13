package com.aeroalga.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BioLime,
    secondary = BioCyan,
    background = BioBackground,
    surface = BioSurface,
    surfaceVariant = BioSurfaceVariant,
    onPrimary = BioBackground,
    onSecondary = BioBackground,
    onBackground = BioTextPrimary,
    onSurface = BioTextPrimary
)

@Composable
fun AeroAlgaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
