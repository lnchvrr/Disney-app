package fr.isen.chevrier.disney_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundBlue,
    surface = CardWhite,
    surfaceVariant = CardWhiteStrong,
    onPrimary = TextOnDark,
    onSecondary = TextOnDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    onSurfaceVariant = TextSecondaryOnDark,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BackgroundBlue,
    surface = CardWhite,
    surfaceVariant = CardWhiteStrong,
    onPrimary = TextOnDark,
    onSecondary = TextOnDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    onSurfaceVariant = TextSecondaryOnDark,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DisneyappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Palette fixe inspirée d'une plateforme de streaming
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