package com.julesmeister.sangfroid.core.designsystem.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

@Immutable
sealed interface CardShapeStyle {

    @Immutable
    data class Folder(
        val tabWidthFraction: Float,
        val tabHeight: Dp,
        val tabSlant: Dp
    ) : CardShapeStyle

    data object Stepped : CardShapeStyle

    data object Ticket : CardShapeStyle
}