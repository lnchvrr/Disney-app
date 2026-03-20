package fr.isen.chevrier.disney_app.ui.theme

import androidx.compose.ui.graphics.Color

/** Fond principal (type plateforme streaming). */
val StreamingBackground = Color(0xFF0B0F1A)

/** Cartes / surfaces surélevées (légèrement plus claires que le fond). */
val StreamingSurface = Color(0xFF151B2E)
val StreamingSurfaceHigh = Color(0xFF1E283D)

/** Accent bleu (CTA, sélection). */
val AccentBlueLight = Color(0xFF3B9EFF)
val AccentBlueLightAlt = Color(0xFF63A8DF)

/** Texte sur fond sombre / cartes sombres. */
val TextPrimaryOnDark = Color(0xFFF0F4FA)
val TextSecondaryOnDark = Color(0xFFB0BCCF)

// Compat anciennes références (UI migrée vers MaterialTheme + couleurs ci-dessus)
val BackgroundBlue = StreamingBackground
val CardWhite = StreamingSurface
val CardWhiteStrong = StreamingSurfaceHigh
val TextOnDark = TextPrimaryOnDark
val TextOnCard = TextPrimaryOnDark

val Purple80 = AccentBlueLight
val PurpleGrey80 = AccentBlueLightAlt
val Pink80 = AccentBlueLight
val Purple40 = AccentBlueLight
val PurpleGrey40 = AccentBlueLightAlt
val Pink40 = AccentBlueLight
