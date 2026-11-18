package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

private const val Wordmark =
    "Sangfroid"

private val CompactHeaderWidth =
    380.dp

private val TaglineLift =
    4.dp

@Composable
fun FeedHeader(
    skippedCount: Int,
    canRestoreSkipped: Boolean,
    onRestoreSkipped: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier =
            modifier.fillMaxWidth()
    ) {
        val compact =
            maxWidth <
                    CompactHeaderWidth

        val taglineLiftPx =
            with(LocalDensity.current) {
                TaglineLift.toPx()
            }

        Column(
            modifier =
                Modifier.fillMaxWidth(),
            verticalArrangement =
                Arrangement.spacedBy(
                    0.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Sm
                    )
            ) {
                HeaderWordmark(
                    text =
                        Wordmark,
                    compact =
                        compact,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                HeaderActionDock(
                    compact =
                        compact,
                    skippedCount =
                        skippedCount,
                    canRestoreSkipped =
                        canRestoreSkipped,
                    onRestoreSkipped =
                        onRestoreSkipped,
                    onOpenSaved =
                        onOpenSaved,
                    onOpenProgress =
                        onOpenProgress,
                    onOpenProfile =
                        onOpenProfile
                )
            }

//            HeaderTagline(
//                modifier =
//                    Modifier
//                        .fillMaxWidth()
//                        .graphicsLayer {
//                            translationY =
//                                -taglineLiftPx
//                        }
//            )
        }
    }
}