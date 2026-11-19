package com.julesmeister.sangfroid.feature.practice.components

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.QuestionCategoryColors
import com.julesmeister.sangfroid.core.designsystem.theme.questionCategoryColors
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.feature.practice.PracticeAttemptActionsViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.delay

private val FolderHeaderHeight = 50.dp
private val FolderTabHeight = 44.dp
private val FolderTabMinWidth = 56.dp
private val FolderQuestionTabMinWidth = 112.dp
private val FolderTabOverlap = 10.dp
private val FolderTabSlant = 14.dp
private val FolderTabRadius = 12.dp
private val FolderBodyRadius = 24.dp
private val FolderConnectionDepth = 8.dp
private val TabScrollLeadIn = 20.dp
private val BodySwipeThreshold = 56.dp

private val ActionTrackHeight = 40.dp
private val ActionTrackPadding = 3.dp
private val ActionDeleteWidth = 40.dp

private const val ContentAnimationMillis = 240
private const val CollapsedAnswerLines = 4
private const val InactiveTabBackgroundBlend = 0.55f

private class ConnectedFolderTabShape(
    private val slant: Dp,
    private val radius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        val slantPx =
            with(density) { slant.toPx() }
                .coerceIn(0f, width / 3f)

        val radiusPx =
            with(density) { radius.toPx() }
                .coerceIn(0f, height / 2f)

        val path =
            Path().apply {
                moveTo(0f, height)
                lineTo(0f, radiusPx)
                quadraticTo(0f, 0f, radiusPx, 0f)
                lineTo(width - slantPx - radiusPx, 0f)
                quadraticTo(
                    width - slantPx,
                    0f,
                    width - slantPx * 0.78f,
                    radiusPx
                )
                cubicTo(
                    width - slantPx * 0.44f,
                    height * 0.34f,
                    width - slantPx * 0.42f,
                    height,
                    width,
                    height
                )
                close()
            }

        return Outline.Generic(path)
    }
}

private enum class TabIndicator {
    CATEGORY,
    NOT_EVALUATED,
    PENDING,
    EVALUATED,
    FAILED,
    DELETING
}

@Composable
fun PracticeQuestionCard(
    question: QuestionEntity,
    attempts: List<PracticeAttemptEntity>,
    selectedAttemptId: String?,
    onSelectCurrentAttempt: () -> Unit,
    onSelectAttempt: (String) -> Unit,
    onSelectPreviousAttempt: () -> Unit,
    onSelectNextAttempt: () -> Unit,
    onUseAttemptAsNewAnswer: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    modifier: Modifier = Modifier,
    actionsViewModel: PracticeAttemptActionsViewModel =
        hiltViewModel()
) {
    val actionsState by
    actionsViewModel
        .state
        .collectAsStateWithLifecycle()

    val colors =
        questionCategoryColors(question.category)

    val orderedAttempts =
        remember(attempts) {
            attempts.sortedWith(
                compareByDescending<PracticeAttemptEntity> {
                    it.submittedAt
                }.thenBy {
                    it.id
                }
            )
        }

    val selectedAttempt =
        orderedAttempts.firstOrNull {
            it.id == selectedAttemptId
        }

    val selectedTabIndex =
        tabIndexOf(
            attemptId = selectedAttempt?.id,
            orderedAttempts = orderedAttempts
        )

    var deleteConfirmationId by
    remember(question.id) {
        mutableStateOf<String?>(null)
    }

    /*
     * The folder body's top-right radius adapts to the trailing tab.
     *
     * When the last tab's slanted foot reaches into the rounded corner,
     * the corner squares off underneath it so the foot stays connected
     * to the body instead of floating over the curve.
     */
    var bodyTopEndRadius by
    remember(question.id) {
        mutableStateOf(FolderBodyRadius)
    }

    val confirmationAttempt =
        orderedAttempts.firstOrNull {
            it.id == deleteConfirmationId
        }

    val entrance =
        remember(question.id) {
            Animatable(0f)
        }

    val density =
        LocalDensity.current

    val layoutDirection =
        LocalLayoutDirection.current

    val entranceOffsetPx =
        with(density) { 18.dp.toPx() }

    val swipeThresholdPx =
        with(density) { BodySwipeThreshold.toPx() }

    LaunchedEffect(question.id) {
        entrance.snapTo(0f)

        delay(50L)

        entrance.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
        )
    }

    LaunchedEffect(actionsState.deletedAttemptId) {
        val deletedId =
            actionsState.deletedAttemptId
                ?: return@LaunchedEffect

        if (selectedAttemptId == deletedId) {
            onSelectCurrentAttempt()
        }

        deleteConfirmationId = null

        actionsViewModel.consumeDeletion()
    }

    if (confirmationAttempt != null) {
        val number =
            attemptNumber(
                attempt = confirmationAttempt,
                orderedAttempts = orderedAttempts
            )

        AlertDialog(
            onDismissRequest = {
                deleteConfirmationId = null
            },
            containerColor =
                MaterialTheme.colorScheme.surface,
            titleContentColor =
                MaterialTheme.colorScheme.onSurface,
            textContentColor =
                MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Delete attempt $number?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                        "This permanently deletes this answer, " +
                                "its feedback, and any saved revisions. " +
                                "Your current draft and other attempts " +
                                "will remain unchanged."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteConfirmationId = null

                        actionsViewModel.deleteAttempt(
                            confirmationAttempt.id
                        )
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor =
                                MaterialTheme.colorScheme.error
                        )
                ) {
                    Text(
                        text = "Delete attempt",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteConfirmationId = null
                    }
                ) {
                    Text(text = "Keep attempt")
                }
            }
        )
    }

    if (actionsState.errorMessage != null) {
        AlertDialog(
            onDismissRequest =
                actionsViewModel::dismissError,
            title = {
                Text(text = "Attempt not deleted")
            },
            text = {
                Text(
                    text =
                        actionsState.errorMessage.orEmpty()
                )
            },
            confirmButton = {
                TextButton(
                    onClick =
                        actionsViewModel::dismissError
                ) {
                    Text(text = "Close")
                }
            }
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val progress = entrance.value

                    alpha = progress.coerceIn(0f, 1f)

                    translationY =
                        (1f - progress) * entranceOffsetPx
                }
    ) {
        FolderAttemptTabs(
            question = question,
            attempts = orderedAttempts,
            selectedTabIndex = selectedTabIndex,
            deletingAttemptIds =
                actionsState.deletingAttemptIds,
            colors = colors,
            onSelectCurrentAttempt =
                onSelectCurrentAttempt,
            onSelectAttempt =
                onSelectAttempt,
            onBodyTopEndRadiusChanged = { radius ->
                bodyTopEndRadius = radius
            }
        )

        val bodyShape =
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = bodyTopEndRadius,
                bottomStart = FolderBodyRadius,
                bottomEnd = FolderBodyRadius
            )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .pointerInput(
                        orderedAttempts.size,
                        swipeThresholdPx,
                        layoutDirection
                    ) {
                        if (orderedAttempts.isEmpty()) {
                            return@pointerInput
                        }

                        var totalDrag = 0f

                        detectHorizontalDragGestures(
                            onDragStart = {
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { change, amount ->
                                totalDrag += amount

                                if (abs(totalDrag) > swipeThresholdPx / 3f) {
                                    change.consume()
                                }
                            },
                            onDragCancel = {
                                totalDrag = 0f
                            },
                            onDragEnd = {
                                if (abs(totalDrag) >= swipeThresholdPx) {
                                    val moveForward =
                                        if (layoutDirection == LayoutDirection.Rtl) {
                                            totalDrag > 0f
                                        } else {
                                            totalDrag < 0f
                                        }

                                    if (moveForward) {
                                        onSelectNextAttempt()
                                    } else {
                                        onSelectPreviousAttempt()
                                    }
                                }

                                totalDrag = 0f
                            }
                        )
                    }
        ) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(bodyShape)
                        .background(colors.container)
            )

            AnimatedCardDecoration(
                colors = colors,
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(bodyShape)
            )

            AnimatedContent(
                targetState =
                    selectedAttempt?.id,
                transitionSpec = {
                    val from =
                        tabIndexOf(
                            attemptId = initialState,
                            orderedAttempts = orderedAttempts
                        )

                    val to =
                        tabIndexOf(
                            attemptId = targetState,
                            orderedAttempts = orderedAttempts
                        )

                    val direction =
                        if (to >= from) 1 else -1

                    (
                            slideInHorizontally(
                                animationSpec =
                                    tween(
                                        ContentAnimationMillis,
                                        easing = FastOutSlowInEasing
                                    )
                            ) { width ->
                                direction * width / 8
                            } + fadeIn(tween(ContentAnimationMillis))
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec =
                                    tween(
                                        ContentAnimationMillis,
                                        easing = FastOutSlowInEasing
                                    )
                            ) { width ->
                                -direction * width / 8
                            } + fadeOut(tween(ContentAnimationMillis / 2))
                            ) using SizeTransform(clip = false)
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 248.dp)
                        .padding(SangfroidSpacing.CardContent),
                label = "folder-content"
            ) { attemptId ->
                val attempt =
                    orderedAttempts.firstOrNull {
                        it.id == attemptId
                    }

                if (attempt == null) {
                    QuestionContent(
                        question = question,
                        attemptCount = orderedAttempts.size,
                        colors = colors
                    )
                } else {
                    HistoricalAttemptContent(
                        question = question,
                        attempt = attempt,
                        attemptNumber =
                            attemptNumber(
                                attempt = attempt,
                                orderedAttempts = orderedAttempts
                            ),
                        colors = colors,
                        isDeleting =
                            attempt.id in
                                    actionsState.deletingAttemptIds,
                        onOpenFeedback = {
                            onOpenFeedback(attempt.id)
                        },
                        onUseAsNewAnswer = {
                            onUseAttemptAsNewAnswer(attempt.id)
                        },
                        onDelete = {
                            deleteConfirmationId = attempt.id
                        }
                    )
                }
            }
        }
    }
}

/*
 * ===========================================================
 * TABS
 * ===========================================================
 */

@Composable
private fun FolderAttemptTabs(
    question: QuestionEntity,
    attempts: List<PracticeAttemptEntity>,
    selectedTabIndex: Int,
    deletingAttemptIds: Set<String>,
    colors: QuestionCategoryColors,
    onSelectCurrentAttempt: () -> Unit,
    onSelectAttempt: (String) -> Unit,
    onBodyTopEndRadiusChanged: (Dp) -> Unit
) {
    val scrollState =
        rememberScrollState()

    val density =
        LocalDensity.current

    val leadInPx =
        with(density) { TabScrollLeadIn.toPx() }

    val slantPx =
        with(density) { FolderTabSlant.toPx() }

    val connectionDepthPx =
        with(density) { FolderConnectionDepth.toPx() }

    val bodyRadiusPx =
        with(density) { FolderBodyRadius.toPx() }

    val tabOffsets =
        remember(question.id, attempts.size) {
            mutableStateMapOf<Int, Int>()
        }

    val tabRightEdges =
        remember(question.id, attempts.size) {
            mutableStateMapOf<Int, Float>()
        }

    var viewportWidthPx by
    remember {
        mutableStateOf(0)
    }

    /*
     * Distance between the trailing tab's visible right foot and the
     * viewport's right edge, capped to the body radius.
     *
     * - Plenty of room: full 24 dp rounded corner.
     * - Trailing tab entering the corner: radius shrinks so the flat
     *   top edge of the body always reaches the foot of the tab.
     * - Trailing tab at or past the edge: square corner.
     *
     * Because this reads scroll position and measured tab edges, the
     * corner follows scrolling and tab-width animations smoothly.
     */
    val trailingCornerRadiusPx by
    remember(
        tabRightEdges,
        scrollState,
        bodyRadiusPx
    ) {
        derivedStateOf {
            val trailingRight =
                tabRightEdges.values.maxOrNull()

            if (
                trailingRight == null ||
                viewportWidthPx <= 0
            ) {
                bodyRadiusPx
            } else {
                val visibleRight =
                    trailingRight - scrollState.value

                (viewportWidthPx - visibleRight)
                    .coerceIn(0f, bodyRadiusPx)
            }
        }
    }

    LaunchedEffect(trailingCornerRadiusPx) {
        onBodyTopEndRadiusChanged(
            with(density) {
                trailingCornerRadiusPx.toDp()
            }
        )
    }

    val inactiveTabColor =
        lerp(
            colors.container,
            MaterialTheme.colorScheme.background,
            InactiveTabBackgroundBlend
        )

    LaunchedEffect(
        selectedTabIndex,
        scrollState.maxValue,
        tabOffsets[selectedTabIndex]
    ) {
        val offset =
            tabOffsets[selectedTabIndex]
                ?: return@LaunchedEffect

        scrollState.animateScrollTo(
            (offset - leadInPx)
                .toInt()
                .coerceIn(0, scrollState.maxValue)
        )
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(FolderHeaderHeight)
                .onSizeChanged { size ->
                    viewportWidthPx = size.width
                }
                .horizontalScroll(scrollState)
                .drawBehind {
                    /*
                     * Joins the tab feet to the folder body.
                     *
                     * The strip stops before the last tab's slanted edge
                     * and never reaches the body's (adaptive) rounded
                     * top-right corner, so nothing sticks out beyond
                     * the card.
                     */
                    val tabsEnd =
                        tabRightEdges.values.maxOrNull()
                            ?: return@drawBehind

                    val visibleLimit =
                        scrollState.value +
                                viewportWidthPx -
                                trailingCornerRadiusPx

                    val stripEnd =
                        min(
                            tabsEnd - slantPx,
                            visibleLimit
                        )

                    if (stripEnd > 0f) {
                        drawRect(
                            color = colors.container,
                            topLeft =
                                Offset(
                                    0f,
                                    size.height - connectionDepthPx
                                ),
                            size =
                                Size(
                                    stripEnd,
                                    connectionDepthPx
                                )
                        )
                    }
                }
                .selectableGroup(),
        verticalAlignment = Alignment.Bottom
    ) {
        FolderTab(
            label =
                question.category.displayName(),
            selected =
                selectedTabIndex == 0,
            isFirstTab = true,
            indicator =
                TabIndicator.CATEGORY,
            colors = colors,
            inactiveColor = inactiveTabColor,
            enabled = true,
            accessibilityLabel = "Question",
            onClick =
                onSelectCurrentAttempt,
            modifier =
                Modifier
                    .zIndex(
                        if (selectedTabIndex == 0) 2f else 0f
                    )
                    .widthIn(min = FolderQuestionTabMinWidth)
                    .onGloballyPositioned { coordinates ->
                        val x =
                            coordinates.positionInParent().x

                        tabOffsets[0] = x.toInt()

                        tabRightEdges[0] =
                            x + coordinates.size.width
                    }
        )

        attempts.forEachIndexed { index, attempt ->
            val tabIndex = index + 1
            val number = attempts.size - index
            val selected = selectedTabIndex == tabIndex
            val deleting = attempt.id in deletingAttemptIds

            FolderTab(
                label =
                    if (selected) {
                        "Attempt $number"
                    } else {
                        "$number"
                    },
                selected = selected,
                isFirstTab = false,
                indicator =
                    if (deleting) {
                        TabIndicator.DELETING
                    } else {
                        attempt.evaluationStatus.tabIndicator()
                    },
                colors = colors,
                inactiveColor = inactiveTabColor,
                enabled = !deleting,
                accessibilityLabel =
                    "Attempt $number, " +
                            attempt.evaluationStatus.displayName(),
                onClick = {
                    onSelectAttempt(attempt.id)
                },
                modifier =
                    Modifier
                        .zIndex(
                            if (selected) {
                                2f
                            } else {
                                1f - tabIndex * 0.001f
                            }
                        )
                        .offset(
                            x = -FolderTabOverlap * tabIndex
                        )
                        .onGloballyPositioned { coordinates ->
                            val x =
                                coordinates.positionInParent().x

                            tabOffsets[tabIndex] = x.toInt()

                            tabRightEdges[tabIndex] =
                                x + coordinates.size.width
                        }
            )
        }

        Spacer(
            modifier = Modifier.width(16.dp)
        )
    }
}

@Composable
private fun FolderTab(
    label: String,
    selected: Boolean,
    isFirstTab: Boolean,
    indicator: TabIndicator,
    colors: QuestionCategoryColors,
    inactiveColor: Color,
    enabled: Boolean,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val height by
    animateDpAsState(
        targetValue =
            if (selected) {
                FolderHeaderHeight
            } else {
                FolderTabHeight
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        label = "folder-tab-height"
    )

    val backgroundColor by
    animateColorAsState(
        targetValue =
            if (selected) {
                colors.container
            } else {
                inactiveColor
            },
        animationSpec = tween(170),
        label = "folder-tab-background"
    )

    val labelColor by
    animateColorAsState(
        targetValue =
            if (selected) {
                colors.content
            } else {
                colors.secondaryContent.copy(alpha = 0.7f)
            },
        animationSpec = tween(170),
        label = "folder-tab-label"
    )

    val selectedMarkerWidth by
    animateDpAsState(
        targetValue =
            if (selected) 22.dp else 0.dp,
        animationSpec = tween(180),
        label = "tab-selected-marker"
    )

    val shape =
        remember {
            ConnectedFolderTabShape(
                slant = FolderTabSlant,
                radius = FolderTabRadius
            )
        }

    Box(
        modifier =
            modifier
                .widthIn(min = FolderTabMinWidth)
                .height(height)
                .clip(shape)
                .background(backgroundColor)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.Tab,
                    onClick = onClick
                )
                .semantics {
                    contentDescription = accessibilityLabel
                }
                .animateContentSize(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                )
                .padding(
                    start =
                        if (isFirstTab) 16.dp else 14.dp,
                    end = FolderTabSlant + 7.dp
                ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TabIndicatorGlyph(
                    indicator = indicator,
                    selected = selected,
                    colors = colors
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight =
                        if (selected) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.SemiBold
                        },
                    color = labelColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Box(
                modifier =
                    Modifier
                        .width(selectedMarkerWidth)
                        .height(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.accent)
            )
        }
    }
}

@Composable
private fun TabIndicatorGlyph(
    indicator: TabIndicator,
    selected: Boolean,
    colors: QuestionCategoryColors
) {
    val errorColor =
        MaterialTheme.colorScheme.error

    when (indicator) {
        TabIndicator.DELETING,
        TabIndicator.PENDING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                color = colors.accent,
                strokeWidth = 1.4.dp
            )
        }

        TabIndicator.CATEGORY,
        TabIndicator.EVALUATED,
        TabIndicator.FAILED,
        TabIndicator.NOT_EVALUATED -> {
            val dotColor =
                when (indicator) {
                    TabIndicator.FAILED ->
                        errorColor.copy(
                            alpha = if (selected) 1f else 0.7f
                        )

                    TabIndicator.NOT_EVALUATED ->
                        colors.secondaryContent.copy(alpha = 0.4f)

                    else ->
                        colors.accent.copy(
                            alpha = if (selected) 1f else 0.55f
                        )
                }

            val hollow =
                indicator == TabIndicator.NOT_EVALUATED

            Canvas(
                modifier =
                    Modifier.size(
                        if (selected) 7.dp else 6.dp
                    )
            ) {
                if (hollow) {
                    drawCircle(
                        color = dotColor,
                        radius =
                            size.minDimension / 2f - 0.6.dp.toPx(),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                } else {
                    drawCircle(color = dotColor)
                }
            }
        }
    }
}

/*
 * ===========================================================
 * QUESTION TAB CONTENT
 * ===========================================================
 */

@Composable
private fun QuestionContent(
    question: QuestionEntity,
    attemptCount: Int,
    colors: QuestionCategoryColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(SangfroidSpacing.Md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR QUESTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = colors.accent
            )

            if (attemptCount > 0) {
                Text(
                    text =
                        if (attemptCount == 1) {
                            "1 attempt"
                        } else {
                            "$attemptCount attempts"
                        },
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        colors.secondaryContent.copy(alpha = 0.76f)
                )
            }
        }

        Text(
            text = question.question,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = colors.content
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            AccentMarker(colors)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "WHY THIS IS ASKED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.secondaryContent
                )

                Text(
                    text = question.whyAsked,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryContent
                )
            }
        }

        question.answerFramework
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { framework ->
                FrameworkPanel(
                    framework = framework,
                    colors = colors
                )
            }
    }
}

/*
 * ===========================================================
 * ATTEMPT TAB CONTENT
 * ===========================================================
 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoricalAttemptContent(
    question: QuestionEntity,
    attempt: PracticeAttemptEntity,
    attemptNumber: Int,
    colors: QuestionCategoryColors,
    isDeleting: Boolean,
    onOpenFeedback: () -> Unit,
    onUseAsNewAnswer: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(SangfroidSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Attempt $attemptNumber",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.content
                )

                Text(
                    text = attempt.submittedAt.relativeLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.secondaryContent
                )
            }

            EvaluationStatusBadge(
                status = attempt.evaluationStatus,
                colors = colors
            )
        }

        Text(
            text = question.question,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = colors.secondaryContent.copy(alpha = 0.8f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        AnswerExcerpt(
            answer = attempt.answerText,
            colors = colors
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetaChip(
                text = attempt.inputMethod.displayName(),
                colors = colors
            )

            MetaChip(
                text =
                    if (attempt.wordCount == 1) {
                        "1 word"
                    } else {
                        "${attempt.wordCount} words"
                    },
                colors = colors
            )

            if (attempt.durationSeconds > 0) {
                MetaChip(
                    text = attempt.durationSeconds.durationLabel(),
                    colors = colors
                )
            }
        }

        AttemptSplitActions(
            status = attempt.evaluationStatus,
            colors = colors,
            isDeleting = isDeleting,
            onOpenFeedback = onOpenFeedback,
            onUseAsNewAnswer = onUseAsNewAnswer,
            onDelete = onDelete
        )
    }
}

@Composable
private fun AnswerExcerpt(
    answer: String,
    colors: QuestionCategoryColors
) {
    var expanded by
    remember(answer) {
        mutableStateOf(false)
    }

    var overflows by
    remember(answer) {
        mutableStateOf(false)
    }

    val isBlankAnswer =
        answer.isBlank()

    val displayedAnswer =
        answer.ifBlank {
            "No answer was recorded."
        }

    /*
     * A lighter surface than the folder body so the recorded answer
     * becomes the focal point of the attempt view.
     */
    val panelColor =
        lerp(
            colors.container,
            MaterialTheme.colorScheme.surface,
            0.72f
        )

    val shape =
        RoundedCornerShape(20.dp)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(panelColor)
                .border(
                    width = 1.dp,
                    color = colors.accent.copy(alpha = 0.22f),
                    shape = shape
                )
                .animateContentSize()
                .padding(
                    start = 16.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 14.dp
                ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuoteGlyph(
                    color = colors.accent
                )

                Text(
                    text = "YOUR ANSWER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.accent
                )
            }

            if (overflows || expanded) {
                Text(
                    text =
                        if (expanded) {
                            "Collapse"
                        } else {
                            "Read all"
                        },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.accent.copy(alpha = 0.1f))
                            .clickable(role = Role.Button) {
                                expanded = !expanded
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        SelectionContainer {
            Text(
                text = displayedAnswer,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        lineHeight =
                            MaterialTheme.typography.bodyMedium.lineHeight * 1.08f
                    ),
                fontStyle =
                    if (isBlankAnswer) {
                        FontStyle.Italic
                    } else {
                        FontStyle.Normal
                    },
                color =
                    if (isBlankAnswer) {
                        colors.secondaryContent.copy(alpha = 0.7f)
                    } else {
                        colors.content
                    },
                maxLines =
                    if (expanded) {
                        Int.MAX_VALUE
                    } else {
                        CollapsedAnswerLines
                    },
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!expanded) {
                        overflows = result.hasVisualOverflow
                    }
                }
            )
        }
    }
}

@Composable
private fun QuoteGlyph(
    color: Color
) {
    Canvas(
        modifier = Modifier.size(width = 12.dp, height = 10.dp)
    ) {
        val radius = size.height * 0.28f
        val stroke = 1.6.dp.toPx()

        listOf(
            size.width * 0.24f,
            size.width * 0.76f
        ).forEach { centerX ->
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(centerX, radius)
            )

            drawLine(
                color = color,
                start = Offset(centerX + radius * 0.7f, radius),
                end = Offset(centerX - radius * 0.4f, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun MetaChip(
    text: String,
    colors: QuestionCategoryColors
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.secondaryContent,
        maxLines = 1,
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/*
 * ===========================================================
 * SPLIT ACTIONS
 * ===========================================================
 */

@Composable
private fun AttemptSplitActions(
    status: EvaluationStatus,
    colors: QuestionCategoryColors,
    isDeleting: Boolean,
    onOpenFeedback: () -> Unit,
    onUseAsNewAnswer: () -> Unit,
    onDelete: () -> Unit
) {
    val feedbackAction =
        status.feedbackAction()

    val pending =
        status == EvaluationStatus.QUEUED ||
                status == EvaluationStatus.PROCESSING

    val feedbackEnabled =
        !isDeleting && feedbackAction.enabled

    val raisedSegmentColor =
        lerp(
            colors.container,
            MaterialTheme.colorScheme.surface,
            0.6f
        )

    val errorColor =
        MaterialTheme.colorScheme.error

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionTrackHeight)
                .clip(RoundedCornerShape(50))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(ActionTrackPadding),
        horizontalArrangement =
            Arrangement.spacedBy(ActionTrackPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SplitActionPill(
            modifier = Modifier.weight(1.35f),
            label = feedbackAction.label,
            enabled = feedbackEnabled,
            containerColor =
                if (feedbackEnabled) {
                    colors.content
                } else {
                    Color.Transparent
                },
            contentColor =
                if (feedbackEnabled) {
                    colors.container
                } else {
                    colors.secondaryContent
                },
            border = null,
            showProgress = pending,
            onClick = onOpenFeedback
        )

        SplitActionPill(
            modifier = Modifier.weight(0.85f),
            label = "Reuse",
            enabled = !isDeleting,
            containerColor = raisedSegmentColor,
            contentColor = colors.content,
            border =
                BorderStroke(
                    1.dp,
                    colors.content.copy(alpha = 0.08f)
                ),
            showProgress = false,
            onClick = onUseAsNewAnswer
        )

        Surface(
            onClick = onDelete,
            enabled = !isDeleting,
            modifier =
                Modifier
                    .width(ActionDeleteWidth)
                    .fillMaxHeight()
                    .semantics {
                        contentDescription = "Delete attempt"
                    },
            shape = CircleShape,
            color = errorColor.copy(alpha = 0.1f),
            contentColor = errorColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = errorColor,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    TrashGlyph(
                        color = errorColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitActionPill(
    label: String,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    showProgress: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
        border = border
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = contentColor,
                    strokeWidth = 1.4.dp
                )

                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrashGlyph(
    color: Color
) {
    Canvas(
        modifier = Modifier.size(15.dp)
    ) {
        val stroke = 1.5.dp.toPx()
        val width = size.width
        val height = size.height

        drawLine(
            color = color,
            start = Offset(width * 0.16f, height * 0.25f),
            end = Offset(width * 0.84f, height * 0.25f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(width * 0.4f, height * 0.11f),
            end = Offset(width * 0.6f, height * 0.11f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        val body =
            Path().apply {
                moveTo(width * 0.25f, height * 0.34f)
                lineTo(width * 0.31f, height * 0.87f)
                lineTo(width * 0.69f, height * 0.87f)
                lineTo(width * 0.75f, height * 0.34f)
            }

        drawPath(
            path = body,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun EvaluationStatusBadge(
    status: EvaluationStatus,
    colors: QuestionCategoryColors
) {
    val errorColor =
        MaterialTheme.colorScheme.error

    val containerColor =
        when (status) {
            EvaluationStatus.COMPLETED ->
                colors.accent.copy(alpha = 0.16f)

            EvaluationStatus.FAILED ->
                errorColor.copy(alpha = 0.12f)

            EvaluationStatus.QUEUED,
            EvaluationStatus.PROCESSING,
            EvaluationStatus.NOT_REQUESTED ->
                colors.content.copy(alpha = 0.08f)
        }

    val contentColor =
        when (status) {
            EvaluationStatus.FAILED ->
                errorColor

            EvaluationStatus.COMPLETED ->
                colors.accent

            EvaluationStatus.QUEUED,
            EvaluationStatus.PROCESSING,
            EvaluationStatus.NOT_REQUESTED ->
                colors.secondaryContent
        }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (
                status == EvaluationStatus.QUEUED ||
                status == EvaluationStatus.PROCESSING
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    color = contentColor,
                    strokeWidth = 1.4.dp
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(contentColor)
                )
            }

            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/*
 * ===========================================================
 * DECORATION
 * ===========================================================
 */

@Composable
private fun AccentMarker(
    colors: QuestionCategoryColors
) {
    val transition =
        rememberInfiniteTransition(label = "question-accent")

    val pulse by
    transition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2_600),
                repeatMode = RepeatMode.Reverse
            ),
        label = "question-accent-pulse"
    )

    Box(
        modifier =
            Modifier
                .padding(top = 3.dp)
                .size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.16f))
        )

        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
        )
    }
}

@Composable
private fun FrameworkPanel(
    framework: String,
    colors: QuestionCategoryColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.content.copy(alpha = 0.08f),
        contentColor = colors.content,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier =
                Modifier
                    .heightIn(min = 64.dp)
                    .padding(SangfroidSpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FrameworkGlyph(colors)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "SUGGESTED STRUCTURE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.secondaryContent
                )

                Text(
                    text = framework,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.content
                )
            }
        }
    }
}

@Composable
private fun FrameworkGlyph(
    colors: QuestionCategoryColors
) {
    Surface(
        modifier = Modifier.size(40.dp),
        color = colors.accent.copy(alpha = 0.16f),
        shape = CircleShape
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(11.dp)
        ) {
            val strokeWidth = 2.dp.toPx()
            val bulletRadius = 1.8.dp.toPx()

            repeat(3) { index ->
                val y = size.height / 3f * index + strokeWidth

                drawCircle(
                    color = colors.accent,
                    radius = bulletRadius,
                    center = Offset(bulletRadius, y)
                )

                drawLine(
                    color = colors.accent,
                    start = Offset(bulletRadius * 3f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun AnimatedCardDecoration(
    colors: QuestionCategoryColors,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(label = "folder-decoration")

    val sweep by
    transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 8_000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "folder-sweep"
    )

    val pulse by
    transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 2_600,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "folder-pulse"
    )

    Canvas(modifier) {
        val radius =
            size.minDimension * (0.18f + pulse * 0.035f)

        val center =
            Offset(
                size.width * (0.82f + sweep * 0.05f),
                size.height * (0.08f + sweep * 0.05f)
            )

        drawCircle(
            color = colors.accent.copy(alpha = 0.05f + pulse * 0.02f),
            radius = radius,
            center = center
        )

        drawCircle(
            color = colors.accent.copy(alpha = 0.07f),
            radius = radius * 0.64f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        repeat(5) { index ->
            val angle =
                (index / 5f + sweep * 0.12f) *
                        (Math.PI * 2).toFloat()

            drawCircle(
                color = colors.accent.copy(alpha = 0.16f),
                radius = (1.5f + pulse).dp.toPx(),
                center =
                    Offset(
                        center.x + cos(angle) * radius * 0.78f,
                        center.y + sin(angle) * radius * 0.78f
                    )
            )
        }
    }
}

/*
 * ===========================================================
 * HELPERS
 * ===========================================================
 */

private fun attemptNumber(
    attempt: PracticeAttemptEntity,
    orderedAttempts: List<PracticeAttemptEntity>
): Int {
    val index =
        orderedAttempts.indexOfFirst {
            it.id == attempt.id
        }

    return if (index >= 0) {
        orderedAttempts.size - index
    } else {
        1
    }
}

private fun tabIndexOf(
    attemptId: String?,
    orderedAttempts: List<PracticeAttemptEntity>
): Int {
    if (attemptId == null) {
        return 0
    }

    val index =
        orderedAttempts.indexOfFirst {
            it.id == attemptId
        }

    return if (index >= 0) index + 1 else 0
}

private data class FeedbackAction(
    val label: String,
    val enabled: Boolean
)

private fun EvaluationStatus.feedbackAction(): FeedbackAction {
    return when (this) {
        EvaluationStatus.NOT_REQUESTED ->
            FeedbackAction("Get feedback", true)

        EvaluationStatus.QUEUED ->
            FeedbackAction("Queued", false)

        EvaluationStatus.PROCESSING ->
            FeedbackAction("Evaluating…", false)

        EvaluationStatus.COMPLETED ->
            FeedbackAction("View feedback", true)

        EvaluationStatus.FAILED ->
            FeedbackAction("Retry feedback", true)
    }
}

private fun EvaluationStatus.tabIndicator(): TabIndicator {
    return when (this) {
        EvaluationStatus.NOT_REQUESTED ->
            TabIndicator.NOT_EVALUATED

        EvaluationStatus.QUEUED,
        EvaluationStatus.PROCESSING ->
            TabIndicator.PENDING

        EvaluationStatus.COMPLETED ->
            TabIndicator.EVALUATED

        EvaluationStatus.FAILED ->
            TabIndicator.FAILED
    }
}

private fun Long.relativeLabel(): String {
    return DateUtils.getRelativeTimeSpanString(
        this,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

private fun Int.durationLabel(): String {
    val safeSeconds = coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60

    return when {
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

private fun AnswerInputMethod.displayName(): String {
    return when (this) {
        AnswerInputMethod.TYPED -> "Typed"
        AnswerInputMethod.SPEECH -> "Voice"
        AnswerInputMethod.MIXED -> "Voice + edits"
    }
}

private fun EvaluationStatus.displayName(): String {
    return when (this) {
        EvaluationStatus.NOT_REQUESTED -> "Not evaluated"
        EvaluationStatus.QUEUED -> "Queued"
        EvaluationStatus.PROCESSING -> "Evaluating"
        EvaluationStatus.COMPLETED -> "Evaluated"
        EvaluationStatus.FAILED -> "Failed"
    }
}

private fun QuestionCategory.displayName(): String {
    return when (this) {
        QuestionCategory.HR -> "HR"
        QuestionCategory.BEHAVIORAL -> "Behavioral"
        QuestionCategory.TECHNICAL -> "Technical"
        QuestionCategory.SYSTEM_DESIGN -> "System design"
        QuestionCategory.DOMAIN -> "Domain"
        QuestionCategory.PROJECT -> "Project"
    }
}