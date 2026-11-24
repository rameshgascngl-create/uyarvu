package org.uyarvu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UyarvuLightColorScheme = lightColorScheme(
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

private val UyarvuDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6CD4DD),
    onPrimary = Color(0xFF00363B),
    primaryContainer = Color(0xFF004F56),
    onPrimaryContainer = Color(0xFFB3ECF0),
    secondary = Color(0xFFB8C4FF),
    onSecondary = Color(0xFF16275B),
    background = Color(0xFF141718),
    onBackground = Color(0xFFE9ECEE),
    surface = Color(0xFF1C2022),
    onSurface = Color(0xFFE9ECEE),
    surfaceVariant = Color(0xFF282D30),
    onSurfaceVariant = Color(0xFFBDC5CC),
    outline = Color(0xFF3E4549)
)

@Composable
fun UyarvuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) UyarvuDarkColorScheme else UyarvuLightColorScheme,
        typography = UyarvuTypography,
        content = content
    )
}
