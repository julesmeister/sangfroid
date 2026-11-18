package com.julesmeister.sangfroid.core.designsystem.shape

import androidx.compose.ui.graphics.Shape
import com.julesmeister.sangfroid.domain.model.QuestionCategory

object SangfroidCardShapes {

    private val HrStyle =
        CardShapeStyle.Folder(
            tabWidthFraction =
                SangfroidShapeDimensions
                    .HrFolderTabFraction,
            tabHeight =
                SangfroidShapeDimensions
                    .FolderTabHeight,
            tabSlant =
                SangfroidShapeDimensions
                    .FolderTabSlant
        )

    private val SystemDesignStyle =
        CardShapeStyle.Folder(
            tabWidthFraction =
                SangfroidShapeDimensions
                    .SystemDesignFolderTabFraction,
            tabHeight =
                SangfroidShapeDimensions
                    .FolderTabHeight,
            tabSlant =
                SangfroidShapeDimensions
                    .FolderTabSlant
        )

    /*
     * DOMAIN questions represent profession-specific knowledge.
     * The stepped shape keeps them visually structured while
     * remaining consistent with the existing card vocabulary.
     */
    private val DomainStyle =
        CardShapeStyle.Stepped

    /*
     * PROJECT questions are evidence-driven and may have
     * historical attempts, so the folder treatment fits the
     * existing "record/history" visual language.
     */
    private val ProjectStyle =
        CardShapeStyle.Folder(
            tabWidthFraction =
                0.38f,
            tabHeight =
                SangfroidShapeDimensions
                    .FolderTabHeight,
            tabSlant =
                SangfroidShapeDimensions
                    .FolderTabSlant
        )

    /*
     * When the question has attempt history, the practice
     * card becomes an actual folder browser regardless of
     * the category's normal decorative card shape.
     *
     * This keeps the category/current tab and historical
     * attempt tabs physically attached to one shared body.
     */
    private val HistoryFolderStyle =
        CardShapeStyle.Folder(
            tabWidthFraction =
                0.34f,
            tabHeight =
                SangfroidShapeDimensions
                    .FolderTabHeight,
            tabSlant =
                SangfroidShapeDimensions
                    .FolderTabSlant
        )

    fun styleFor(
        category: QuestionCategory
    ): CardShapeStyle {
        return when (
            category
        ) {
            QuestionCategory.HR ->
                HrStyle

            QuestionCategory.BEHAVIORAL ->
                CardShapeStyle.Ticket

            QuestionCategory.TECHNICAL ->
                CardShapeStyle.Stepped

            QuestionCategory.SYSTEM_DESIGN ->
                SystemDesignStyle

            QuestionCategory.DOMAIN ->
                DomainStyle

            QuestionCategory.PROJECT ->
                ProjectStyle
        }
    }

    fun practiceStyleFor(
        category: QuestionCategory,
        hasAttempts: Boolean
    ): CardShapeStyle {
        if (hasAttempts) {
            return when (
                category
            ) {
                QuestionCategory.HR ->
                    HrStyle

                QuestionCategory.SYSTEM_DESIGN ->
                    SystemDesignStyle

                QuestionCategory.PROJECT ->
                    ProjectStyle

                QuestionCategory.BEHAVIORAL,
                QuestionCategory.TECHNICAL,
                QuestionCategory.DOMAIN ->
                    HistoryFolderStyle
            }
        }

        return styleFor(
            category
        )
    }

    fun folderStyleFor(
        category: QuestionCategory
    ): CardShapeStyle.Folder {
        return when (
            category
        ) {
            QuestionCategory.HR ->
                HrStyle

            QuestionCategory.SYSTEM_DESIGN ->
                SystemDesignStyle

            QuestionCategory.PROJECT ->
                ProjectStyle

            QuestionCategory.BEHAVIORAL,
            QuestionCategory.TECHNICAL,
            QuestionCategory.DOMAIN ->
                HistoryFolderStyle
        }
    }

    fun shapeFor(
        style: CardShapeStyle
    ): Shape {
        return when (
            style
        ) {
            is CardShapeStyle.Folder ->
                FolderCardShape(
                    tabWidthFraction =
                        style.tabWidthFraction,
                    tabHeight =
                        style.tabHeight,
                    tabSlant =
                        style.tabSlant,
                    cornerRadius =
                        SangfroidRadius.Card,
                    tabCornerRadius =
                        SangfroidShapeDimensions
                            .FolderTabCornerRadius
                )

            CardShapeStyle.Stepped ->
                SteppedCardShape(
                    stepWidth =
                        SangfroidShapeDimensions
                            .StepWidth,
                    stepHeight =
                        SangfroidShapeDimensions
                            .StepHeight,
                    outerRadius =
                        SangfroidRadius.Card,
                    innerRadius =
                        SangfroidRadius.Small
                )

            CardShapeStyle.Ticket ->
                TicketCardShape(
                    cornerRadius =
                        SangfroidRadius.Card,
                    notchRadius =
                        SangfroidShapeDimensions
                            .TicketNotchRadius,
                    notchVerticalFraction =
                        SangfroidShapeDimensions
                            .TicketNotchFraction
                )
        }
    }

    fun forCategory(
        category: QuestionCategory
    ): Shape {
        return shapeFor(
            styleFor(
                category
            )
        )
    }

    fun forPracticeCard(
        category: QuestionCategory,
        hasAttempts: Boolean
    ): Shape {
        return shapeFor(
            practiceStyleFor(
                category =
                    category,
                hasAttempts =
                    hasAttempts
            )
        )
    }
}