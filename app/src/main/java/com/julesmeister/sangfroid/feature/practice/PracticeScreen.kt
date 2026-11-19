// app/src/main/java/com/julesmeister/sangfroid/feature/practice/PracticeScreen.kt

package com.julesmeister.sangfroid.feature.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.feature.practice.components.PracticeBottomDock
import com.julesmeister.sangfroid.feature.practice.components.PracticeLoadingState
import com.julesmeister.sangfroid.feature.practice.components.PracticeMissingQuestion
import com.julesmeister.sangfroid.feature.practice.components.PracticeScrollableContent
import com.julesmeister.sangfroid.feature.practice.speech.SpeechUiState

@Composable
fun PracticeScreen(
    onNavigateBack: () -> Unit,
    onOpenFeedback: (String) -> Unit,
    revisionRequest: String? = null,
    onRevisionRequestConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PracticeViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val speechUiState by
    viewModel
        .speechUiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        revisionRequest
    ) {
        val script =
            revisionRequest
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return@LaunchedEffect

        viewModel.useRevisionAsGuide(
            script
        )

        onRevisionRequestConsumed()
    }

    LaunchedEffect(
        uiState.pendingNavigationAttemptId
    ) {
        val attemptId =
            uiState.pendingNavigationAttemptId
                ?: return@LaunchedEffect

        onOpenFeedback(
            attemptId
        )

        viewModel.consumeFeedbackNavigation()
    }

    val context =
        LocalContext.current

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) { granted ->
            if (granted) {
                viewModel.startSpeechRecording()
            }
        }

    val requestSpeechRecording:
                () -> Unit = {
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) ==
                    PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startSpeechRecording()
        } else {
            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    PracticeContent(
        modifier =
            modifier,
        uiState =
            uiState,
        speechUiState =
            speechUiState,
        onNavigateBack =
            onNavigateBack,
        onAnswerChanged =
            viewModel::updateAnswer,
        onSelectCurrentAttempt =
            viewModel::selectCurrentAttempt,
        onSelectAttempt =
            viewModel::selectAttempt,
        onSelectPreviousAttempt =
            viewModel::selectPreviousAttempt,
        onSelectNextAttempt =
            viewModel::selectNextAttempt,
        onUseAttemptAsNewAnswer =
            viewModel::useAttemptAsNewAnswer,
        onOpenFeedback =
            onOpenFeedback,
        onShowIdealAnswer =
            viewModel::showIdealAnswer,
        onRefreshIdealAnswer =
            viewModel::refreshIdealAnswer,
        onStartRecording =
            requestSpeechRecording,
        onStopRecording =
            viewModel::stopSpeechRecording,
        onCancelRecording =
            viewModel::cancelSpeechRecording,
        onSubmit =
            viewModel::submitAnswer
    )
}

@OptIn(
    ExperimentalLayoutApi::class
)
@Composable
private fun PracticeContent(
    uiState: PracticeUiState,
    speechUiState: SpeechUiState,
    onNavigateBack: () -> Unit,
    onAnswerChanged: (String) -> Unit,
    onSelectCurrentAttempt: () -> Unit,
    onSelectAttempt: (String) -> Unit,
    onSelectPreviousAttempt: () -> Unit,
    onSelectNextAttempt: () -> Unit,
    onUseAttemptAsNewAnswer: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onShowIdealAnswer: () -> Unit,
    onRefreshIdealAnswer: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isKeyboardVisible =
        WindowInsets.isImeVisible

    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    var isEditorFocused by
    remember {
        mutableStateOf(
            false
        )
    }

    /*
     * Clicking Ideal answer or Refresh can transfer focus away from the
     * BasicTextField before the ViewModel publishes isGeneratingIdealAnswer.
     *
     * Without this local pin, compact becomes false for one composition.
     * PracticeScrollableContent then switches back to its normal scroll
     * layout, whose initial position is the top of the screen.
     *
     * Set this before invoking the ViewModel so the editor remains expanded
     * continuously, including the frame before generation state arrives.
     */
    var isIdealAnswerRequestPinned by
    remember {
        mutableStateOf(
            false
        )
    }

    /*
     * Records whether the ViewModel has actually entered generation.
     * This prevents the local pin from being removed immediately while
     * isGeneratingIdealAnswer is still false during the first frame.
     */
    var hasObservedIdealAnswerGeneration by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        uiState.isGeneratingIdealAnswer
    ) {
        if (
            uiState.isGeneratingIdealAnswer
        ) {
            hasObservedIdealAnswerGeneration =
                true

            isIdealAnswerRequestPinned =
                true
        } else if (
            hasObservedIdealAnswerGeneration
        ) {
            hasObservedIdealAnswerGeneration =
                false

            isIdealAnswerRequestPinned =
                false
        }
    }

    val isEditing =
        isEditorFocused ||
                isKeyboardVisible

    val isVoiceActive =
        speechUiState.isRecording ||
                speechUiState.isProcessing

    /*
     * Keep PracticeScrollableContent in its expanded editor layout for the
     * complete ideal-answer request. This preserves the visible editor top
     * instead of recreating the normal scrollable layout at scroll position 0.
     */
    val shouldKeepEditorExpanded =
        isEditing ||
                isVoiceActive ||
                isIdealAnswerRequestPinned ||
                uiState.isGeneratingIdealAnswer

    val isInteractionActive =
        isEditing ||
                isVoiceActive ||
                isIdealAnswerRequestPinned ||
                uiState.isGeneratingIdealAnswer ||
                uiState.isViewingHistoricalAttempt

    val dismissEditor:
                () -> Unit = {
        focusManager.clearFocus(
            force =
                true
        )

        keyboardController?.hide()

        isEditorFocused =
            false
    }

    val selectCurrentAttempt:
                () -> Unit = {
        dismissEditor()

        onSelectCurrentAttempt()
    }

    val selectHistoricalAttempt:
                (String) -> Unit = { attemptId ->
        dismissEditor()

        onSelectAttempt(
            attemptId
        )
    }

    val requestIdealAnswer:
                () -> Unit = {
        if (
            !uiState.isGeneratingIdealAnswer
        ) {
            /*
             * Pin first, then invoke the ViewModel.
             *
             * The ordering is intentional. It prevents the click-induced
             * focus loss from collapsing the editor before loading begins.
             */
            isIdealAnswerRequestPinned =
                true

            hasObservedIdealAnswerGeneration =
                false

            onShowIdealAnswer()
        }
    }

    val refreshIdealAnswer:
                () -> Unit = {
        if (
            !uiState.isGeneratingIdealAnswer
        ) {
            /*
             * Refresh uses the same local pin. The current typed answer,
             * current ghost guide, and editor position remain visible while
             * the replacement guide is generated.
             */
            isIdealAnswerRequestPinned =
                true

            hasObservedIdealAnswerGeneration =
                false

            onRefreshIdealAnswer()
        }
    }

    val handleBack:
                () -> Unit = {
        when {
            speechUiState.isRecording -> {
                onCancelRecording()

                dismissEditor()
            }

            speechUiState.isProcessing -> {
                Unit
            }

            uiState.isGeneratingIdealAnswer ||
                    isIdealAnswerRequestPinned -> {
                /*
                 * Do not collapse or navigate while the remote request is
                 * running. The ViewModel owns cancellation/lifecycle cleanup.
                 */
                Unit
            }

            isEditing -> {
                dismissEditor()
            }

            uiState.isViewingHistoricalAttempt -> {
                selectCurrentAttempt()
            }

            else -> {
                onNavigateBack()
            }
        }
    }

    BackHandler(
        enabled =
            isInteractionActive
    ) {
        handleBack()
    }

    SangfroidScreen(
        modifier =
            modifier,
        applyVerticalPadding =
            false
    ) {
        when {
            uiState.isLoading -> {
                PracticeLoadingState()
            }

            uiState.question == null -> {
                PracticeMissingQuestion(
                    onNavigateBack =
                        onNavigateBack
                )
            }

            else -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .imePadding()
                ) {
                    PracticeScrollableContent(
                        uiState =
                            uiState,
                        speechUiState =
                            speechUiState,
                        onNavigateBack =
                            handleBack,
                        onAnswerChanged =
                            onAnswerChanged,
                        onSelectCurrentAttempt =
                            selectCurrentAttempt,
                        onSelectAttempt =
                            selectHistoricalAttempt,
                        onSelectPreviousAttempt = {
                            dismissEditor()

                            onSelectPreviousAttempt()
                        },
                        onSelectNextAttempt = {
                            dismissEditor()

                            onSelectNextAttempt()
                        },
                        onUseAttemptAsNewAnswer = { attemptId ->
                            dismissEditor()

                            onUseAttemptAsNewAnswer(
                                attemptId
                            )
                        },
                        onOpenFeedback = { attemptId ->
                            dismissEditor()

                            onOpenFeedback(
                                attemptId
                            )
                        },
                        onEditorFocusChanged = { focused ->
                            isEditorFocused =
                                focused
                        },
                        onShowIdealAnswer =
                            requestIdealAnswer,
                        onRefreshIdealAnswer =
                            refreshIdealAnswer,
                        compact =
                            shouldKeepEditorExpanded,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                )
                    )

                    PracticeBottomDock(
                        uiState =
                            uiState,
                        speechUiState =
                            speechUiState,
                        onStartRecording = {
                            dismissEditor()

                            onStartRecording()
                        },
                        onStopRecording =
                            onStopRecording,
                        onCancelRecording = {
                            onCancelRecording()

                            dismissEditor()
                        },
                        onSubmit = {
                            dismissEditor()

                            onSubmit()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top =
                                        SangfroidSpacing.Xs,
                                    bottom =
                                        SangfroidSpacing.Xs
                                )
                    )
                }
            }
        }
    }
}