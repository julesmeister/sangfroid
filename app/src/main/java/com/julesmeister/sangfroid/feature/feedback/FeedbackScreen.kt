package com.julesmeister.sangfroid.feature.feedback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.feature.feedback.components.RevisionDetailSheet
import com.julesmeister.sangfroid.feature.feedback.components.RevisionHistorySheet

@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    onTryAgain: (String) -> Unit,
    onUseRevision: (
        questionId: String,
        revision: SavedRevision
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    FeedbackContent(
        uiState =
            uiState,
        onNavigateBack =
            onNavigateBack,
        onTryAgain = {
            uiState.question
                ?.id
                ?.let(
                    onTryAgain
                )
        },
        onRetryEvaluation =
            viewModel::retry,
        onSaveRevision =
            viewModel::saveImprovedRevision,
        onOpenRevisionHistory =
            viewModel::showRevisionHistory,
        modifier =
            modifier
    )

    if (
        uiState.isRevisionHistoryVisible &&
        uiState.selectedRevision == null &&
        uiState.revisionPendingDeletion == null
    ) {
        RevisionHistorySheet(
            revisions =
                uiState.revisions,
            onRevisionSelected =
                viewModel::openRevision,
            onDismiss =
                viewModel::hideRevisionHistory
        )
    }

    uiState.selectedRevision
        ?.let { revision ->
            RevisionDetailSheet(
                revision =
                    revision,
                isDeleting =
                    uiState.isDeletingRevision,
                errorMessage =
                    uiState.revisionErrorMessage,
                onUseRevision = {
                    val questionId =
                        uiState.question
                            ?.id
                            ?: return@RevisionDetailSheet

                    viewModel.hideRevisionHistory()

                    onUseRevision(
                        questionId,
                        revision
                    )
                },
                onDelete = {
                    viewModel
                        .requestRevisionDeletion(
                            revision
                        )
                },
                onDismiss =
                    viewModel::closeRevision
            )
        }

    uiState.revisionPendingDeletion
        ?.let { revision ->
            AlertDialog(
                onDismissRequest =
                    viewModel::cancelRevisionDeletion,
                title = {
                    Text(
                        text =
                            "Delete version ${revision.versionNumber}?",
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text =
                            "This saved revision will be removed permanently. Other versions will remain available."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick =
                            viewModel::confirmRevisionDeletion,
                        enabled =
                            !uiState.isDeletingRevision
                    ) {
                        Text(
                            text =
                                if (
                                    uiState.isDeletingRevision
                                ) {
                                    "Deleting…"
                                } else {
                                    "Delete"
                                },
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick =
                            viewModel::cancelRevisionDeletion,
                        enabled =
                            !uiState.isDeletingRevision
                    ) {
                        Text(
                            text =
                                "Cancel"
                        )
                    }
                }
            )
        }
}