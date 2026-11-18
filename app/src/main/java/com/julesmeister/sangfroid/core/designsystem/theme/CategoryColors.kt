package com.julesmeister.sangfroid.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.julesmeister.sangfroid.domain.model.QuestionCategory

@Immutable
data class QuestionCategoryColors(
    val container: Color,
    val accent: Color,
    val content: Color,
    val secondaryContent: Color,
    val tagContainer: Color,
    val tagContent: Color
)

@Composable
fun questionCategoryColors(
    category: QuestionCategory,
    darkTheme: Boolean = isSystemInDarkTheme()
): QuestionCategoryColors {
    return if (darkTheme) {
        darkQuestionCategoryColors(
            category
        )
    } else {
        lightQuestionCategoryColors(
            category
        )
    }
}

private fun lightQuestionCategoryColors(
    category: QuestionCategory
): QuestionCategoryColors {
    return when (category) {
        QuestionCategory.HR -> {
            QuestionCategoryColors(
                container =
                    HrLightContainer,
                accent =
                    HrLightAccent,
                content =
                    HrLightContent,
                secondaryContent =
                    HrLightSecondaryContent,
                tagContainer =
                    HrLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }

        QuestionCategory.BEHAVIORAL -> {
            QuestionCategoryColors(
                container =
                    BehavioralLightContainer,
                accent =
                    BehavioralLightAccent,
                content =
                    BehavioralLightContent,
                secondaryContent =
                    BehavioralLightSecondaryContent,
                tagContainer =
                    BehavioralLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }

        QuestionCategory.TECHNICAL -> {
            QuestionCategoryColors(
                container =
                    TechnicalLightContainer,
                accent =
                    TechnicalLightAccent,
                content =
                    TechnicalLightContent,
                secondaryContent =
                    TechnicalLightSecondaryContent,
                tagContainer =
                    TechnicalLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }

        QuestionCategory.SYSTEM_DESIGN -> {
            QuestionCategoryColors(
                container =
                    SystemDesignLightContainer,
                accent =
                    SystemDesignLightAccent,
                content =
                    SystemDesignLightContent,
                secondaryContent =
                    SystemDesignLightSecondaryContent,
                tagContainer =
                    SystemDesignLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }

        QuestionCategory.DOMAIN -> {
            QuestionCategoryColors(
                container =
                    DomainLightContainer,
                accent =
                    DomainLightAccent,
                content =
                    DomainLightContent,
                secondaryContent =
                    DomainLightSecondaryContent,
                tagContainer =
                    DomainLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }

        QuestionCategory.PROJECT -> {
            QuestionCategoryColors(
                container =
                    ProjectLightContainer,
                accent =
                    ProjectLightAccent,
                content =
                    ProjectLightContent,
                secondaryContent =
                    ProjectLightSecondaryContent,
                tagContainer =
                    ProjectLightAccent,
                tagContent =
                    SangfroidOffWhite
            )
        }
    }
}

private fun darkQuestionCategoryColors(
    category: QuestionCategory
): QuestionCategoryColors {
    return when (category) {
        QuestionCategory.HR -> {
            QuestionCategoryColors(
                container =
                    HrDarkContainer,
                accent =
                    HrDarkAccent,
                content =
                    HrDarkContent,
                secondaryContent =
                    HrDarkSecondaryContent,
                tagContainer =
                    HrDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }

        QuestionCategory.BEHAVIORAL -> {
            QuestionCategoryColors(
                container =
                    BehavioralDarkContainer,
                accent =
                    BehavioralDarkAccent,
                content =
                    BehavioralDarkContent,
                secondaryContent =
                    BehavioralDarkSecondaryContent,
                tagContainer =
                    BehavioralDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }

        QuestionCategory.TECHNICAL -> {
            QuestionCategoryColors(
                container =
                    TechnicalDarkContainer,
                accent =
                    TechnicalDarkAccent,
                content =
                    TechnicalDarkContent,
                secondaryContent =
                    TechnicalDarkSecondaryContent,
                tagContainer =
                    TechnicalDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }

        QuestionCategory.SYSTEM_DESIGN -> {
            QuestionCategoryColors(
                container =
                    SystemDesignDarkContainer,
                accent =
                    SystemDesignDarkAccent,
                content =
                    SystemDesignDarkContent,
                secondaryContent =
                    SystemDesignDarkSecondaryContent,
                tagContainer =
                    SystemDesignDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }

        QuestionCategory.DOMAIN -> {
            QuestionCategoryColors(
                container =
                    DomainDarkContainer,
                accent =
                    DomainDarkAccent,
                content =
                    DomainDarkContent,
                secondaryContent =
                    DomainDarkSecondaryContent,
                tagContainer =
                    DomainDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }

        QuestionCategory.PROJECT -> {
            QuestionCategoryColors(
                container =
                    ProjectDarkContainer,
                accent =
                    ProjectDarkAccent,
                content =
                    ProjectDarkContent,
                secondaryContent =
                    ProjectDarkSecondaryContent,
                tagContainer =
                    ProjectDarkAccent,
                tagContent =
                    SangfroidCharcoal
            )
        }
    }
}

private val DomainLightContainer =
    Color(
        0xFFE8F5F2
    )

private val DomainLightAccent =
    Color(
        0xFF167D73
    )

private val DomainLightContent =
    Color(
        0xFF123D39
    )

private val DomainLightSecondaryContent =
    Color(
        0xFF52736F
    )

private val DomainDarkContainer =
    Color(
        0xFF183E3A
    )

private val DomainDarkAccent =
    Color(
        0xFF70D6CA
    )

private val DomainDarkContent =
    Color(
        0xFFE5FAF7
    )

private val DomainDarkSecondaryContent =
    Color(
        0xFFA8CBC6
    )

private val ProjectLightContainer =
    Color(
        0xFFF1ECFA
    )

private val ProjectLightAccent =
    Color(
        0xFF7552A6
    )

private val ProjectLightContent =
    Color(
        0xFF3D285D
    )

private val ProjectLightSecondaryContent =
    Color(
        0xFF716283
    )

private val ProjectDarkContainer =
    Color(
        0xFF35294A
    )

private val ProjectDarkAccent =
    Color(
        0xFFC9A7F5
    )

private val ProjectDarkContent =
    Color(
        0xFFF5EEFF
    )

private val ProjectDarkSecondaryContent =
    Color(
        0xFFC9BADB
    )
