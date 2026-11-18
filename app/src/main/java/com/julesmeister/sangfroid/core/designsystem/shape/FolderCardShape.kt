package com.julesmeister.sangfroid.core.designsystem.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.min

class FolderCardShape(
    private val tabWidthFraction: Float,
    private val tabHeight: Dp,
    private val tabSlant: Dp,
    private val cornerRadius: Dp,
    private val tabCornerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width =
            size.width

        val height =
            size.height

        val tabHeightPx =
            with(density) {
                tabHeight.toPx()
            }
                .coerceAtMost(
                    height / 4f
                )

        val radius =
            with(density) {
                cornerRadius.toPx()
            }
                .coerceAtMost(
                    min(
                        width,
                        height - tabHeightPx
                    ) / 4f
                )

        val tabRadius =
            with(density) {
                tabCornerRadius.toPx()
            }
                .coerceAtMost(
                    tabHeightPx
                )

        val slant =
            with(density) {
                tabSlant.toPx()
            }

        val minimumTabEnd =
            tabRadius +
                    slant

        val maximumTabEnd =
            max(
                minimumTabEnd,
                width -
                        radius *
                        2f
            )

        val tabEnd =
            (
                    width *
                            tabWidthFraction
                    )
                .coerceIn(
                    minimumTabEnd,
                    maximumTabEnd
                )

        val tabFlatEnd =
            tabEnd -
                    slant

        val path =
            Path().apply {
                moveTo(
                    0f,
                    tabRadius
                )

                arcTo(
                    rect =
                        Rect(
                            left = 0f,
                            top = 0f,
                            right =
                                tabRadius *
                                        2f,
                            bottom =
                                tabRadius *
                                        2f
                        ),
                    startAngleDegrees =
                        180f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    tabFlatEnd,
                    0f
                )

                cubicTo(
                    x1 =
                        tabFlatEnd +
                                slant *
                                0.55f,
                    y1 =
                        0f,
                    x2 =
                        tabEnd -
                                slant *
                                0.55f,
                    y2 =
                        tabHeightPx,
                    x3 =
                        tabEnd,
                    y3 =
                        tabHeightPx
                )

                lineTo(
                    width -
                            radius,
                    tabHeightPx
                )

                arcTo(
                    rect =
                        Rect(
                            left =
                                width -
                                        radius *
                                        2f,
                            top =
                                tabHeightPx,
                            right =
                                width,
                            bottom =
                                tabHeightPx +
                                        radius *
                                        2f
                        ),
                    startAngleDegrees =
                        270f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    width,
                    height -
                            radius
                )

                arcTo(
                    rect =
                        Rect(
                            left =
                                width -
                                        radius *
                                        2f,
                            top =
                                height -
                                        radius *
                                        2f,
                            right =
                                width,
                            bottom =
                                height
                        ),
                    startAngleDegrees =
                        0f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                lineTo(
                    radius,
                    height
                )

                arcTo(
                    rect =
                        Rect(
                            left =
                                0f,
                            top =
                                height -
                                        radius *
                                        2f,
                            right =
                                radius *
                                        2f,
                            bottom =
                                height
                        ),
                    startAngleDegrees =
                        90f,
                    sweepAngleDegrees =
                        90f,
                    forceMoveTo =
                        false
                )

                close()
            }

        return Outline.Generic(
            path
        )
    }
}