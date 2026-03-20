package fr.isen.chevrier.disney_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueLight,
    onPrimary = Color(0xFF001018),
    primaryContainer = Color(0xFF1A3A5C),
    onPrimaryContainer = TextPrimaryOnDark,
    secondary = AccentBlueLightAlt,
    onSecondary = Color(0xFF001018),
    tertiary = AccentBlueLight,
    background = StreamingBackground,
    onBackground = TextPrimaryOnDark,
    surface = StreamingSurface,
    onSurface = TextPrimaryOnDark,
    surfaceVariant = StreamingSurfaceHigh,
    onSurfaceVariant = TextSecondaryOnDark,
    outline = Color(0xFF3D4A63),
    outlineVariant = Color(0xFF2A3448),
    surfaceContainerLowest = StreamingBackground,
    surfaceContainerLow = StreamingSurface,
    surfaceContainer = StreamingSurface,
    surfaceContainerHigh = StreamingSurfaceHigh,
    surfaceContainerHighest = Color(0xFF28334A),
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlueLight,
    secondary = AccentBlueLightAlt,
    tertiary = AccentBlueLight,
    background = StreamingBackground,
    surface = StreamingSurface,
    onBackground = TextPrimaryOnDark,
    onSurface = TextPrimaryOnDark,
)

@Composable
fun DisneyappTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
