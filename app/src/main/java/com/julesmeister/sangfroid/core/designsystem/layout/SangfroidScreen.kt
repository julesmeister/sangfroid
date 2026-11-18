package com.julesmeister.sangfroid.core.designsystem.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun SangfroidScreen(
    modifier: Modifier = Modifier,
    applyHorizontalPadding: Boolean = true,
    applyVerticalPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
            )
            .then(
                if (applyHorizontalPadding) {
                    Modifier.padding(
                        horizontal = SangfroidSpacing.ScreenHorizontal
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (applyVerticalPadding) {
                    Modifier.padding(
                        vertical = SangfroidSpacing.ScreenVertical
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}