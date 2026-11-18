package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

internal enum class HeaderGlyph {
    SAVED,
    PROGRESS,
    PROFILE
}

internal fun DrawScope.drawHeaderGlyph(
    glyph: HeaderGlyph,
    color: Color,
    strokeWidth: Float
) {
    when (glyph) {
        HeaderGlyph.SAVED -> {
            drawHeaderBookmarkGlyph(
                color =
                    color,
                strokeWidth =
                    strokeWidth
            )
        }

        HeaderGlyph.PROGRESS -> {
            drawHeaderTrendGlyph(
                color =
                    color,
                strokeWidth =
                    strokeWidth
            )
        }

        HeaderGlyph.PROFILE -> {
            drawHeaderPersonGlyph(
                color =
                    color,
                strokeWidth =
                    strokeWidth
            )
        }
    }
}

private fun DrawScope.drawHeaderBookmarkGlyph(
    color: Color,
    strokeWidth: Float
) {
    val left =
        size.width * 0.24f

    val right =
        size.width * 0.76f

    val top =
        size.height * 0.10f

    val bottom =
        size.height * 0.90f

    val notch =
        size.height * 0.66f

    val path =
        Path().apply {
            moveTo(
                left,
                top
            )

            lineTo(
                right,
                top
            )

            lineTo(
                right,
                bottom
            )

            lineTo(
                size.width / 2f,
                notch
            )

            lineTo(
                left,
                bottom
            )

            close()
        }

    drawPath(
        path =
            path,
        color =
            color,
        style =
            Stroke(
                width =
                    strokeWidth,
                cap =
                    StrokeCap.Round,
                join =
                    StrokeJoin.Round
            )
    )
}

private fun DrawScope.drawHeaderTrendGlyph(
    color: Color,
    strokeWidth: Float
) {
    val points =
        listOf(
            Offset(
                size.width * 0.08f,
                size.height * 0.80f
            ),
            Offset(
                size.width * 0.36f,
                size.height * 0.50f
            ),
            Offset(
                size.width * 0.58f,
                size.height * 0.64f
            ),
            Offset(
                size.width * 0.88f,
                size.height * 0.22f
            )
        )

    val path =
        Path().apply {
            moveTo(
                points.first().x,
                points.first().y
            )

            points
                .drop(1)
                .forEach { point ->
                    lineTo(
                        point.x,
                        point.y
                    )
                }
        }

    drawPath(
        path =
            path,
        color =
            color,
        style =
            Stroke(
                width =
                    strokeWidth,
                cap =
                    StrokeCap.Round,
                join =
                    StrokeJoin.Round
            )
    )

    drawCircle(
        color =
            color,
        radius =
            strokeWidth * 1.5f,
        center =
            points.last()
    )
}

private fun DrawScope.drawHeaderPersonGlyph(
    color: Color,
    strokeWidth: Float
) {
    val width =
        size.width

    val height =
        size.height

    drawCircle(
        color =
            color,
        radius =
            width * 0.2f,
        center =
            Offset(
                x =
                    width / 2f,
                y =
                    height * 0.32f
            ),
        style =
            Stroke(
                width =
                    strokeWidth
            )
    )

    drawArc(
        color =
            color,
        startAngle =
            200f,
        sweepAngle =
            140f,
        useCenter =
            false,
        topLeft =
            Offset(
                x =
                    width * 0.15f,
                y =
                    height * 0.6f
            ),
        size =
            Size(
                width =
                    width * 0.7f,
                height =
                    height * 0.7f
            ),
        style =
            Stroke(
                width =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )
    )
}

internal fun DrawScope.drawHeaderRefreshGlyph(
    color: Color,
    strokeWidth: Float
) {
    val radius =
        (size.minDimension - strokeWidth * 2f) / 2f

    val startAngle =
        40f

    val sweepAngle =
        280f

    drawArc(
        color =
            color,
        startAngle =
            startAngle,
        sweepAngle =
            sweepAngle,
        useCenter =
            false,
        topLeft =
            Offset(
                x =
                    center.x - radius,
                y =
                    center.y - radius
            ),
        size =
            Size(
                radius * 2f,
                radius * 2f
            ),
        style =
            Stroke(
                width =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )
    )

    val endRadians =
        Math.toRadians(
            (startAngle + sweepAngle).toDouble()
        )

    val cosine =
        cos(endRadians).toFloat()

    val sine =
        sin(endRadians).toFloat()

    val arcEnd =
        Offset(
            x =
                center.x + radius * cosine,
            y =
                center.y + radius * sine
        )

    val tangent =
        Offset(
            -sine,
            cosine
        )

    val normal =
        Offset(
            cosine,
            sine
        )

    val head =
        radius * 0.6f

    val tip =
        arcEnd +
                tangent * (head * 0.35f)

    val outerWing =
        arcEnd -
                tangent * (head * 0.55f) +
                normal * (head * 0.6f)

    val innerWing =
        arcEnd -
                tangent * (head * 0.55f) -
                normal * (head * 0.6f)

    drawLine(
        color =
            color,
        start =
            outerWing,
        end =
            tip,
        strokeWidth =
            strokeWidth,
        cap =
            StrokeCap.Round
    )

    drawLine(
        color =
            color,
        start =
            innerWing,
        end =
            tip,
        strokeWidth =
            strokeWidth,
        cap =
            StrokeCap.Round
    )
}