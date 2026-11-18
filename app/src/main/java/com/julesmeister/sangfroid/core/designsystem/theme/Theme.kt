package com.julesmeister.sangfroid.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidShapes
import com.julesmeister.sangfroid.core.designsystem.typography.FontScale
import com.julesmeister.sangfroid.core.designsystem.typography.sangfroidTypography

private val SangfroidLightColorScheme = lightColorScheme(
    primary = SangfroidCharcoal,
    onPrimary = SangfroidOffWhite,

    secondary = SangfroidLime,
    onSecondary = SangfroidCharcoal,

    tertiary = SangfroidCyan,
    onTertiary = SangfroidCharcoal,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,

    outline = LightOutline,

    error = FeedbackError
)

private val SangfroidDarkColorScheme = darkColorScheme(
    primary = SangfroidLime,
    onPrimary = SangfroidCharcoal,

    secondary = SangfroidCyan,
    onSecondary = SangfroidCharcoal,

    tertiary = SangfroidLimeDark,
    onTertiary = SangfroidCharcoal,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkOutline,

    error = FeedbackError
)

@Composable
fun SangfroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: FontScale = FontScale.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        SangfroidDarkColorScheme
    } else {
        SangfroidLightColorScheme
    }

    val typography = sangfroidTypography(
        fontScale = fontScale
    )

    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme

            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = SangfroidShapes,
        content = content
    )
}