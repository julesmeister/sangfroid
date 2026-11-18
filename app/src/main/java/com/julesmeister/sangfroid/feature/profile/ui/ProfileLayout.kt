// app/src/main/java/com/julesmeister/sangfroid/feature/profile/ui/ProfileLayout.kt

package com.julesmeister.sangfroid.feature.profile.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidProfileTokens
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyanDeep
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.feature.feedback.components.RoseOrbitLoader
import com.julesmeister.sangfroid.feature.profile.components.ProfileButton
import com.julesmeister.sangfroid.feature.profile.components.ProfileButtonVariant
import com.julesmeister.sangfroid.feature.profile.components.ProfileIcon
import com.julesmeister.sangfroid.feature.profile.components.ProfileIconView
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val MaxContentWidth =
    640.dp

private const val COMPACT_WIDTH_DP =
    380

private const val UNDO_TIMEOUT_MILLIS =
    4_000L

@Composable
fun ProfileLayout(
    model: ProfileScreenModel,
    actions: ProfileActions,
    modifier: Modifier = Modifier
) {
    val compact =
        LocalConfiguration
            .current
            .screenWidthDp <
                COMPACT_WIDTH_DP

    val cardPadding =
        if (compact) {
            14.dp
        } else {
            18.dp
        }

    val sectionGap =
        if (compact) {
            10.dp
        } else {
            14.dp
        }

    SangfroidScreen(
        modifier =
            modifier
    ) {
        ProfileHeader(
            completeness =
                model.completeness,
            onBack =
                actions.onBack
        )

        Spacer(
            modifier =
                Modifier.height(
                    sectionGap
                )
        )

        if (model.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                RoseOrbitLoader()
            }

            return@SangfroidScreen
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    ),
            contentAlignment =
                Alignment.TopCenter
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(
                            max =
                                MaxContentWidth
                        )
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        sectionGap
                    )
            ) {
                CvSection(
                    model =
                        model.cv,
                    padding =
                        cardPadding,
                    onChooseCv =
                        actions.onChooseCv,
                    onApply =
                        actions.onApplyCv,
                    onDismiss =
                        actions.onDismissCvMessage
                )

                TargetSection(
                    model =
                        model.target,
                    padding =
                        cardPadding,
                    onRoleChange =
                        actions.onRoleChange,
                    onLevelSelect =
                        actions.onLevelSelect
                )

                SkillsSection(
                    model =
                        model.skills,
                    padding =
                        cardPadding,
                    onAdd =
                        actions.onAddSkill,
                    onRemove =
                        actions.onRemoveSkill
                )

                model.cvAnalysis?.let { cvAnalysis ->
                    CvProfileSections(
                        model =
                            cvAnalysis,
                        padding =
                            cardPadding
                    )
                }

                ErrorBanner(
                    message =
                        model.errorMessage
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )
            }
        }

        Box(
            modifier =
                Modifier.fillMaxWidth(),
            contentAlignment =
                Alignment.TopCenter
        ) {
            BottomBar(
                undo =
                    model.undo,
                save =
                    model.save,
                actions =
                    actions,
                modifier =
                    Modifier
                        .widthIn(
                            max =
                                MaxContentWidth
                        )
                        .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    completeness: Float,
    onBack: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        44.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    )
                    .clickable(
                        role =
                            Role.Button,
                        onClickLabel =
                            "Go back",
                        onClick =
                            onBack
                    )
                    .semantics {
                        contentDescription =
                            "Go back"
                    },
            contentAlignment =
                Alignment.Center
        ) {
            ProfileIconView(
                icon =
                    ProfileIcon.Back,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                iconSize =
                    16.dp
            )
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    "Interview profile",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    "Shapes the questions we recommend",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        CompletenessRing(
            progress =
                completeness
        )
    }
}

@Composable
private fun CompletenessRing(
    progress: Float
) {
    val animatedProgress by
    animateFloatAsState(
        targetValue =
            progress.coerceIn(
                0f,
                1f
            ),
        animationSpec =
            spring(
                dampingRatio =
                    Spring
                        .DampingRatioLowBouncy,
                stiffness =
                    Spring
                        .StiffnessLow
            ),
        label =
            "profile-completeness"
    )

    val arcColor =
        if (isSystemInDarkTheme()) {
            SangfroidLime
        } else {
            SangfroidCyanDeep
        }

    val trackColor =
        MaterialTheme
            .colorScheme
            .onSurface
            .copy(
                alpha =
                    0.1f
            )

    val percent =
        (
                animatedProgress *
                        100
                )
            .roundToInt()

    Box(
        modifier =
            Modifier
                .size(
                    SangfroidProfileTokens
                        .CompletenessRingSize
                )
                .semantics {
                    contentDescription =
                        "Profile $percent percent complete"
                },
        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier =
                Modifier.matchParentSize()
        ) {
            val stroke =
                SangfroidProfileTokens
                    .CompletenessRingStroke
                    .toPx()

            val diameter =
                size.minDimension -
                        stroke

            drawCircle(
                color =
                    trackColor,
                radius =
                    diameter /
                            2f,
                style =
                    Stroke(
                        width =
                            stroke
                    )
            )

            drawArc(
                color =
                    arcColor,
                startAngle =
                    -90f,
                sweepAngle =
                    360f *
                            animatedProgress,
                useCenter =
                    false,
                topLeft =
                    Offset(
                        stroke /
                                2f,
                        stroke /
                                2f
                    ),
                size =
                    Size(
                        diameter,
                        diameter
                    ),
                style =
                    Stroke(
                        width =
                            stroke,
                        cap =
                            StrokeCap.Round
                    )
            )
        }

        Text(
            text =
                "$percent%",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String?
) {
    AnimatedVisibility(
        visible =
            message != null,
        enter =
            fadeIn() +
                    expandVertically(),
        exit =
            fadeOut() +
                    shrinkVertically()
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                MaterialTheme
                    .shapes
                    .large,
            color =
                MaterialTheme
                    .colorScheme
                    .errorContainer,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
        ) {
            Text(
                text =
                    message.orEmpty(),
                modifier =
                    Modifier.padding(
                        14.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun BottomBar(
    undo: UndoModel?,
    save: SaveBarModel,
    actions: ProfileActions,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(
        undo?.id
    ) {
        val current =
            undo
                ?: return@LaunchedEffect

        delay(
            UNDO_TIMEOUT_MILLIS
        )

        actions.onUndoExpired(
            current.id
        )
    }

    Column(
        modifier =
            modifier.padding(
                top =
                    8.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        AnimatedContent(
            targetState =
                undo,
            contentKey = { item ->
                item?.id
            },
            transitionSpec = {
                (
                        fadeIn() togetherWith
                                fadeOut()
                        )
                    .using(
                        SizeTransform(
                            clip =
                                false
                        )
                    )
            },
            label =
                "undo-bar"
        ) { item ->
            if (item != null) {
                UndoBar(
                    message =
                        item.message,
                    onUndo =
                        actions.onUndoRemove
                )
            } else {
                Spacer(
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }

        save.hint?.let { hint ->
            Text(
                text =
                    hint,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        ProfileButton(
            label =
                save.label,
            onClick =
                actions.onSave,
            enabled =
                save.enabled,
            loading =
                save.loading,
            icon =
                if (save.showCheck) {
                    ProfileIcon.Check
                } else {
                    ProfileIcon.None
                }
        )
    }
}

@Composable
private fun UndoBar(
    message: String,
    onUndo: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            MaterialTheme
                .shapes
                .large,
        color =
            MaterialTheme
                .colorScheme
                .inverseSurface,
        contentColor =
            MaterialTheme
                .colorScheme
                .inverseOnSurface
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start =
                        14.dp,
                    end =
                        6.dp,
                    top =
                        6.dp,
                    bottom =
                        6.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                text =
                    message,
                modifier =
                    Modifier.weight(
                        1f
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis
            )

            ProfileButton(
                label =
                    "Undo",
                onClick =
                    onUndo,
                variant =
                    ProfileButtonVariant.Secondary,
                fillWidth =
                    false
            )
        }
    }
}