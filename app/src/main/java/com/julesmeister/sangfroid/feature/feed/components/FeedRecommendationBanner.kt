package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.data.recommendation.QuestionRecommendationScore

internal object FeedRecommendationLayout {

    val MinimumHeight =
        60.dp

    val MaximumHeight =
        76.dp

    val GapBelow =
        8.dp

    val HorizontalInset =
        4.dp
}

@Composable
internal fun FeedRecommendationBanner(
    recommendation: QuestionRecommendationScore,
    modifier: Modifier = Modifier
) {
    val containerColor =
        MaterialTheme
            .colorScheme
            .surface

    val contentColor =
        MaterialTheme
            .colorScheme
            .onSurface

    val supportingColor =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant

    val accentContainer =
        MaterialTheme
            .colorScheme
            .primary

    val accentContent =
        MaterialTheme
            .colorScheme
            .onPrimary

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        FeedRecommendationLayout
                            .MinimumHeight,
                    max =
                        FeedRecommendationLayout
                            .MaximumHeight
                ),
        color =
            containerColor,
        contentColor =
            contentColor,
        shape =
            MaterialTheme
                .shapes
                .large,
        tonalElevation =
            2.dp,
        shadowElevation =
            2.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing.Md,
                    vertical =
                        SangfroidSpacing.Sm
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xs
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Sm
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    percent =
                                        50
                                )
                            )
                            .background(
                                accentContainer
                            )
                            .padding(
                                horizontal =
                                    SangfroidSpacing.Sm,
                                vertical =
                                    SangfroidSpacing.Xxs
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "FOR YOU",
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        fontWeight =
                            FontWeight.ExtraBold,
                        color =
                            accentContent,
                        maxLines =
                            1,
                        softWrap =
                            false
                    )
                }

                Text(
                    text =
                        recommendation.displayLabel,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        contentColor,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Text(
                text =
                    recommendation.supportingLabel,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    supportingColor,
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}