package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

internal fun DrawScope.drawCheckGlyph(
    color: Color,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start =
            Offset(
                x = size.width * 0.14f,
                y = size.height * 0.53f
            ),
        end =
            Offset(
                x = size.width * 0.41f,
                y = size.height * 0.78f
            ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start =
            Offset(
                x = size.width * 0.41f,
                y = size.height * 0.78f
            ),
        end =
            Offset(
                x = size.width * 0.88f,
                y = size.height * 0.22f
            ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}