package com.julesmeister.sangfroid.feature.feed.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import kotlin.math.abs

@Stable
class SwipeCardState internal constructor() {

    val offset = Animatable(
        initialValue = Offset.Zero,
        typeConverter = Offset.VectorConverter
    )

    val horizontalProgress: Float
        get() = offset.value.x

    val verticalProgress: Float
        get() = offset.value.y

    suspend fun dragBy(
        dragAmount: Offset
    ) {
        offset.snapTo(
            targetValue = offset.value + dragAmount
        )
    }

    suspend fun settle(
        horizontalThresholdPx: Float,
        verticalThresholdPx: Float,
        containerWidthPx: Float,
        containerHeightPx: Float
    ): SwipeAction? {
        val currentOffset = offset.value

        return when {
            currentOffset.y <= -verticalThresholdPx &&
                    abs(currentOffset.y) > abs(currentOffset.x) -> {
                animateExit(
                    action = SwipeAction.PRACTICE,
                    containerWidthPx = containerWidthPx,
                    containerHeightPx = containerHeightPx
                )

                SwipeAction.PRACTICE
            }

            currentOffset.x >= horizontalThresholdPx -> {
                animateExit(
                    action = SwipeAction.SAVE,
                    containerWidthPx = containerWidthPx,
                    containerHeightPx = containerHeightPx
                )

                SwipeAction.SAVE
            }

            currentOffset.x <= -horizontalThresholdPx -> {
                animateExit(
                    action = SwipeAction.SKIP,
                    containerWidthPx = containerWidthPx,
                    containerHeightPx = containerHeightPx
                )

                SwipeAction.SKIP
            }

            else -> {
                reset()
                null
            }
        }
    }

    suspend fun dismiss(
        action: SwipeAction,
        containerWidthPx: Float,
        containerHeightPx: Float
    ) {
        animateExit(
            action = action,
            containerWidthPx = containerWidthPx,
            containerHeightPx = containerHeightPx
        )
    }

    suspend fun reset() {
        offset.animateTo(
            targetValue = Offset.Zero,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    suspend fun snapBack() {
        offset.snapTo(
            targetValue = Offset.Zero
        )
    }

    private suspend fun animateExit(
        action: SwipeAction,
        containerWidthPx: Float,
        containerHeightPx: Float
    ) {
        val target = when (action) {
            SwipeAction.SKIP -> {
                Offset(
                    x = -containerWidthPx * 1.6f,
                    y = offset.value.y
                )
            }

            SwipeAction.SAVE -> {
                Offset(
                    x = containerWidthPx * 1.6f,
                    y = offset.value.y
                )
            }

            SwipeAction.PRACTICE -> {
                Offset(
                    x = offset.value.x,
                    y = -containerHeightPx * 1.25f
                )
            }
        }

        offset.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 260
            )
        )
    }
}

@Composable
fun rememberSwipeCardState(): SwipeCardState {
    return remember {
        SwipeCardState()
    }
}