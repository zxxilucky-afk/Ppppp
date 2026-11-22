package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage

private val PinkoraLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = PureWhite,
    primaryContainer = PinkLight,
    onPrimaryContainer = DarkText,
    secondary = LavenderDeep,
    onSecondary = PureWhite,
    secondaryContainer = LavenderSurface,
    onSecondaryContainer = DarkText,
    tertiary = MintWellness,
    onTertiary = PureWhite,
    tertiaryContainer = MintLight,
    onTertiaryContainer = DarkText,
    background = BlushBackground,
    onBackground = DarkText,
    surface = PureWhite,
    onSurface = DarkText,
    surfaceVariant = PinkLight,
    onSurfaceVariant = MutedText,
    outline = SoftOutline,
    error = AlertRed,
    onError = PureWhite,
    errorContainer = AlertRedLight,
    onErrorContainer = AlertRed,
)

private val PinkoraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun PinkoraTheme(
    language: AppLanguage = AppLanguage.EN,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PinkoraLightColorScheme,
        typography = pinkoraTypography(language),
        shapes = PinkoraShapes,
        content = content,
    )
}
