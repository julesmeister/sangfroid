package com.julesmeister.sangfroid.core.designsystem.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object SangfroidRadius {

    val None = 0.dp
    val XSmall = 8.dp
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val Huge = 40.dp

    val Card = XLarge
    val CompactCard = Large
    val Button = Medium
    val Tag = Small
    val Dialog = Large
    val Sheet = XLarge

    val Pill = 999.dp
}

object SangfroidShapeDimensions {

    // Folder cards
    val FolderTabHeight = 44.dp
    val FolderTabSlant = 28.dp
    val FolderTabCornerRadius = 18.dp

    const val HrFolderTabFraction = 0.40f
    const val SystemDesignFolderTabFraction = 0.62f

    // Stepped cards
    val StepWidth = 76.dp
    val StepHeight = 60.dp

    // Ticket cards
    val TicketNotchRadius = 14.dp
    const val TicketNotchFraction = 0.68f
}

val SangfroidShapes = Shapes(
    extraSmall = RoundedCornerShape(SangfroidRadius.XSmall),
    small = RoundedCornerShape(SangfroidRadius.Small),
    medium = RoundedCornerShape(SangfroidRadius.Medium),
    large = RoundedCornerShape(SangfroidRadius.Large),
    extraLarge = RoundedCornerShape(SangfroidRadius.XLarge)
)