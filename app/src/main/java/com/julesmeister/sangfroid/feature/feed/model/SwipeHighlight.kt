package com.julesmeister.sangfroid.feature.feed.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

@Immutable
data class SwipeHighlight(
    val action: SwipeAction?,
    val progress: Float
) {

    fun progressFor(target: SwipeAction): Float {
        return if (action == target) progress else 0f
    }

    companion object {

        val None = SwipeHighlight(
            action = null,
            progress = 0f
        )

        fun from(
            offset: Offset,
            horizontalThresholdPx: Float,
            verticalThresholdPx: Float
        ): SwipeHighlight {
            val horizontal = (abs(offset.x) / horizontalThresholdPx)
                .coerceIn(0f, 1f)

            val vertical = (-offset.y / verticalThresholdPx)
                .coerceIn(0f, 1f)

            return when {
                offset.y < 0f && vertical > horizontal -> {
                    SwipeHighlight(SwipeAction.PRACTICE, vertical)
                }

                offset.x > 0f -> SwipeHighlight(SwipeAction.SAVE, horizontal)
                offset.x < 0f -> SwipeHighlight(SwipeAction.SKIP, horizontal)
                else -> None
            }
        }
    }
}