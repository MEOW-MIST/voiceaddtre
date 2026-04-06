package com.roly.eldersdesktop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EldersColorScheme = lightColorScheme(
    primary = EldersPrimary,
    onPrimary = EldersOnPrimary,
    primaryContainer = EldersPrimaryContainer,
    onPrimaryContainer = EldersOnPrimaryContainer,
    secondary = EldersSecondary,
    onSecondary = EldersOnSecondary,
    secondaryContainer = EldersSecondaryContainer,
    onSecondaryContainer = EldersOnSecondaryContainer,
    tertiary = EldersTertiary,
    onTertiary = EldersOnTertiary,
    tertiaryContainer = EldersTertiaryContainer,
    onTertiaryContainer = EldersOnTertiaryContainer,
    error = EldersError,
    onError = EldersOnError,
    errorContainer = EldersErrorContainer,
    onErrorContainer = EldersOnErrorContainer,
    background = EldersBackground,
    onBackground = EldersOnBackground,
    surface = EldersSurface,
    onSurface = EldersOnSurface,
    surfaceVariant = EldersSurfaceVariant,
    onSurfaceVariant = EldersOnSurfaceVariant,
    outline = EldersOutline,
    outlineVariant = EldersOutlineVariant
)

@Composable
fun EldersdesktopTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EldersColorScheme,
        typography = Typography,
        shapes = EldersShapes,
        content = content
    )
}
