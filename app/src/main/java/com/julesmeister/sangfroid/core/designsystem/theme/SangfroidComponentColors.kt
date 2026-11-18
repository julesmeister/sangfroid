package com.julesmeister.sangfroid.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

private val DockLightContainer = SangfroidCharcoal
private val DockLightButton = Color(0xFF34363A)
private val DockDarkContainer = Color(0xFF2A2C2E)
private val DockDarkButton = Color(0xFF3A3D3F)

@Immutable
data class SwipeDockColors(
    val container: Color,
    val buttonContainer: Color,
    val buttonContent: Color,
    val highlightContent: Color,
    val primaryContainer: Color,
    val primaryContent: Color,
    val skipAccent: Color,
    val saveAccent: Color
)

@Composable
fun swipeDockColors(
    darkTheme: Boolean = isSystemInDarkTheme()
): SwipeDockColors {
    return if (darkTheme) {
        SwipeDockColors(
            container = DockDarkContainer,
            buttonContainer = DockDarkButton,
            buttonContent = DarkTextPrimary,
            highlightContent = SangfroidCharcoal,
            primaryContainer = SangfroidCyan,
            primaryContent = SangfroidCharcoal,
            skipAccent = FeedbackError,
            saveAccent = SangfroidLime
        )
    } else {
        SwipeDockColors(
            container = DockLightContainer,
            buttonContainer = DockLightButton,
            buttonContent = SangfroidOffWhite,
            highlightContent = SangfroidCharcoal,
            primaryContainer = SangfroidCyan,
            primaryContent = SangfroidCharcoal,
            skipAccent = FeedbackError,
            saveAccent = SangfroidLime
        )
    }
}