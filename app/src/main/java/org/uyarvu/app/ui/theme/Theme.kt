package org.uyarvu.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UyarvuColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = PrimaryTealContainer,
    onPrimaryContainer = PrimaryTeal,
    secondary = SecondaryIndigo,
    onSecondary = Color.White,
    secondaryContainer = SecondaryIndigoContainer,
    onSecondaryContainer = SecondaryIndigo,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun UyarvuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UyarvuColorScheme,
        typography = UyarvuTypography,
        content = content
    )
}
