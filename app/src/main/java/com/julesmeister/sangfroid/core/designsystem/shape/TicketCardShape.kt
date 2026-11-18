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

class TicketCardShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp,
    private val notchVerticalFraction: Float = 0.72f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        val corner = with(density) {
            cornerRadius.toPx()
        }.coerceAtMost(min(width, height) / 3f)

        val notch = with(density) {
            notchRadius.toPx()
        }.coerceAtMost(width / 6f)

        val notchCenterY = (
                height * notchVerticalFraction.coerceIn(
                    minimumValue = 0.35f,
                    maximumValue = 0.85f
                )
                ).coerceIn(
                minimumValue = corner + notch,
                maximumValue = height - corner - notch
            )

        val path = Path().apply {
            moveTo(corner, 0f)

            lineTo(width - corner, 0f)

            arcTo(
                rect = Rect(
                    left = width - corner * 2f,
                    top = 0f,
                    right = width,
                    bottom = corner * 2f
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(width, notchCenterY - notch)

            arcTo(
                rect = Rect(
                    left = width - notch,
                    top = notchCenterY - notch,
                    right = width + notch,
                    bottom = notchCenterY + notch
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            lineTo(width, height - corner)

            arcTo(
                rect = Rect(
                    left = width - corner * 2f,
                    top = height - corner * 2f,
                    right = width,
                    bottom = height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(corner, height)

            arcTo(
                rect = Rect(
                    left = 0f,
                    top = height - corner * 2f,
                    right = corner * 2f,
                    bottom = height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(0f, notchCenterY + notch)

            arcTo(
                rect = Rect(
                    left = -notch,
                    top = notchCenterY - notch,
                    right = notch,
                    bottom = notchCenterY + notch
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )

            lineTo(0f, corner)

            arcTo(
                rect = Rect(
                    left = 0f,
                    top = 0f,
                    right = corner * 2f,
                    bottom = corner * 2f
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