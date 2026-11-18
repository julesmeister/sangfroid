package com.julesmeister.sangfroid.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.feature.profile.ui.ProfileActions
import com.julesmeister.sangfroid.feature.profile.ui.ProfileLayout

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val model =
        remember(
            uiState
        ) {
            uiState.toScreenModel()
        }

    val cvLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .OpenDocument()
        ) { uri ->
            uri?.let { selectedUri ->
                viewModel.importCv(
                    selectedUri
                )
            }
        }

    val actions =
        remember(
            viewModel,
            cvLauncher,
            onNavigateBack
        ) {
            ProfileActions(
                onBack =
                    onNavigateBack,
                onRoleChange =
                    viewModel::updateTargetRole,
                onLevelSelect =
                    viewModel::updateExperienceLevel,
                onAddSkill =
                    viewModel::addSkillByKey,
                onRemoveSkill =
                    viewModel::removeSkillByKey,
                onUndoRemove =
                    viewModel::undoRemoveSkill,
                onUndoExpired =
                    viewModel::clearRemovedSkill,
                onChooseCv = {
                    cvLauncher.launch(
                        arrayOf(
                            PDF_MIME_TYPE
                        )
                    )
                },
                onApplyCv =
                    viewModel::applyCvProfile,
                onDismissCvMessage =
                    viewModel::dismissCvMessage,
                onSave =
                    viewModel::save
            )
        }

    ProfileLayout(
        model =
            model,
        actions =
            actions,
        modifier =
            modifier
    )
}

private const val PDF_MIME_TYPE =
    "application/pdf"