package com.julesmeister.sangfroid.core.designsystem.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

class SteppedCardShape(
    private val stepWidth: Dp,
    private val stepHeight: Dp,
    private val outerRadius: Dp,
    private val innerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        val outer = with(density) {
            outerRadius.toPx()
        }.coerceAtMost(
            min(width, height) / 3f
        )

        val inner = with(density) {
            innerRadius.toPx()
        }.coerceAtMost(outer)

        val stepWidthPx = with(density) {
            stepWidth.toPx()
        }.coerceIn(
            minimumValue = outer * 2f,
            maximumValue = width * 0.55f
        )

        val stepHeightPx = with(density) {
            stepHeight.toPx()
        }.coerceIn(
            minimumValue = outer,
            maximumValue = height * 0.28f
        )

        val stepStart = width - stepWidthPx

        val path = Path().apply {
            moveTo(
                outer,
                0f
            )

            lineTo(
                stepStart - inner,
                0f
            )

            quadraticBezierTo(
                stepStart,
                0f,
                stepStart,
                inner
            )

            lineTo(
                stepStart,
                stepHeightPx - inner
            )

            quadraticBezierTo(
                stepStart,
                stepHeightPx,
                stepStart + inner,
                stepHeightPx
            )

            lineTo(
                width - outer,
                stepHeightPx
            )

            arcTo(
                rect = Rect(
                    left = width - outer * 2f,
                    top = stepHeightPx,
                    right = width,
                    bottom = stepHeightPx + outer * 2f
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                width,
                height - outer
            )

            arcTo(
                rect = Rect(
                    left = width - outer * 2f,
                    top = height - outer * 2f,
                    right = width,
                    bottom = height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                outer,
                height
            )

            arcTo(
                rect = Rect(
                    left = 0f,
                    top = height - outer * 2f,
                    right = outer * 2f,
                    bottom = height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(
                0f,
                outer
            )

            arcTo(
                rect = Rect(
                    left = 0f,
                    top = 0f,
                    right = outer * 2f,
                    bottom = outer * 2f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }

        return Outline.Generic(path)
    }
}