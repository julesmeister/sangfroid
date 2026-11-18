package com.julesmeister.sangfroid.feature.profile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.cv.CvDocumentReader
import com.julesmeister.sangfroid.data.cv.CvImportResult
import com.julesmeister.sangfroid.domain.cv.AnalyzeCvUseCase
import com.julesmeister.sangfroid.domain.cv.CvAnalysisRepository
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource
import com.julesmeister.sangfroid.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val profileRepository: ProfileRepository,
    private val cvAnalysisRepository: CvAnalysisRepository,
    private val cvDocumentReader: CvDocumentReader,
    private val analyzeCvUseCase: AnalyzeCvUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            ProfileUiState()
        )

    val uiState:
            StateFlow<ProfileUiState> =
        mutableUiState.asStateFlow()

    private var cvImportJob: Job? =
        null

    init {
        observeProfile()
        restoreSavedCvProfile()
    }

    fun updateTargetRole(
        value: String
    ) {
        markEdited { state ->
            state.copy(
                targetRole =
                    value
            )
        }
    }

    fun updateExperienceLevel(
        value: String
    ) {
        markEdited { state ->
            state.copy(
                experienceLevel =
                    value
            )
        }
    }

    fun addSkillByKey(
        key: String
    ) {
        SkillCatalog
            .fromKey(
                key
            )
            ?.let(
                ::addSkill
            )
    }

    fun removeSkillByKey(
        key: String
    ) {
        val code =
            SkillCatalog.fromKey(
                key
            )

        if (code != null) {
            removeSkill(
                code
            )

            return
        }

        SkillCatalog
            .customNameFromKey(
                key
            )
            ?.let(
                ::removeCustomSkill
            )
    }

    fun addSkill(
        skillCode: SkillCode
    ) {
        markEdited { state ->
            if (
                skillCode in
                state.skills
            ) {
                state
            } else {
                state.copy(
                    skills =
                        state.skills +
                                (
                                        skillCode to
                                                ProfileSkillSource.MANUAL
                                        ),
                    recentlyRemoved =
                        state.recentlyRemoved
                            ?.takeUnless { removed ->
                                removed.skillCode ==
                                        skillCode
                            }
                )
            }
        }
    }

    fun removeSkill(
        skillCode: SkillCode
    ) {
        markEdited { state ->
            val source =
                state.skills[
                    skillCode
                ]
                    ?: return@markEdited state

            state.copy(
                skills =
                    state.skills -
                            skillCode,
                recentlyRemoved =
                    RemovedSkill(
                        skillCode =
                            skillCode,
                        source =
                            source
                    )
            )
        }
    }

    fun addCustomSkill(
        name: String
    ) {
        val cleaned =
            CustomSkill.clean(
                name
            )

        if (cleaned.isBlank()) {
            return
        }

        markEdited { state ->
            val key =
                CustomSkill.normalize(
                    cleaned
                )

            if (
                key in
                state.customSkillKeys
            ) {
                state
            } else {
                state.copy(
                    customSkills =
                        state.customSkills +
                                CustomSkill(
                                    cleaned,
                                    ProfileSkillSource.MANUAL
                                ),
                    recentlyRemoved =
                        state.recentlyRemoved
                            ?.takeUnless { removed ->
                                removed
                                    .customSkill
                                    ?.normalizedName ==
                                        key
                            }
                )
            }
        }
    }

    private fun removeCustomSkill(
        normalizedName: String
    ) {
        markEdited { state ->
            val skill =
                state.customSkills
                    .firstOrNull { customSkill ->
                        customSkill.normalizedName ==
                                normalizedName
                    }
                    ?: return@markEdited state

            state.copy(
                customSkills =
                    state.customSkills -
                            skill,
                recentlyRemoved =
                    RemovedSkill(
                        customSkill =
                            skill,
                        source =
                            skill.source
                    )
            )
        }
    }

    fun undoRemoveSkill() {
        markEdited { state ->
            val removed =
                state.recentlyRemoved
                    ?: return@markEdited state

            val code =
                removed.skillCode

            val custom =
                removed.customSkill

            when {
                code != null &&
                        code !in state.skills -> {
                    state.copy(
                        skills =
                            state.skills +
                                    (
                                            code to
                                                    removed.source
                                            ),
                        recentlyRemoved =
                            null
                    )
                }

                custom != null &&
                        custom.normalizedName !in
                        state.customSkillKeys -> {
                    state.copy(
                        customSkills =
                            state.customSkills +
                                    custom,
                        recentlyRemoved =
                            null
                    )
                }

                else -> {
                    state.copy(
                        recentlyRemoved =
                            null
                    )
                }
            }
        }
    }

    fun clearRemovedSkill(
        id: Long
    ) {
        mutableUiState.update { state ->
            if (
                state.recentlyRemoved
                    ?.id ==
                id
            ) {
                state.copy(
                    recentlyRemoved =
                        null
                )
            } else {
                state
            }
        }
    }

    fun importCv(
        uri: Uri
    ) {
        if (
            mutableUiState
                .value
                .cvImport
                .isWorking
        ) {
            return
        }

        cvImportJob?.cancel()

        cvImportJob =
            viewModelScope.launch {
                val fileName =
                    resolveFileName(
                        uri
                    )

                mutableUiState.update { state ->
                    state.copy(
                        cvApplySummary =
                            null
                    )
                }

                updateCvImportState(
                    CvImportUiState(
                        status =
                            CvImportStatus.ReadingPdf,
                        fileName =
                            fileName
                    )
                )

                try {
                    when (
                        val importResult =
                            cvDocumentReader.read(
                                uri
                            )
                    ) {
                        is CvImportResult.Success -> {
                            analyzeExtractedCv(
                                fileName =
                                    fileName,
                                importResult =
                                    importResult
                            )
                        }

                        is CvImportResult.InsufficientText -> {
                            updateCvImportState(
                                CvImportUiState(
                                    status =
                                        CvImportStatus.InsufficientText,
                                    fileName =
                                        fileName,
                                    pageCount =
                                        importResult.pageCount,
                                    characterCount =
                                        importResult.characterCount,
                                    wordCount =
                                        importResult.wordCount,
                                    errorMessage =
                                        importResult.reason
                                )
                            )
                        }

                        is CvImportResult.Failure -> {
                            updateCvImportState(
                                CvImportUiState(
                                    status =
                                        CvImportStatus.Error,
                                    fileName =
                                        fileName,
                                    errorMessage =
                                        importResult.message
                                )
                            )
                        }
                    }
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    updateCvImportState(
                        CvImportUiState(
                            status =
                                CvImportStatus.Error,
                            fileName =
                                fileName,
                            errorMessage =
                                throwable.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: DEFAULT_IMPORT_ERROR
                        )
                    )
                }
            }
    }

    fun applyCvProfile() {
        val cvProfile =
            mutableUiState
                .value
                .activeCvProfile
                ?: return

        val resolved =
            cvProfile.resolveSkillCodes()

        markEdited { state ->
            val previousCodes =
                state.selectedSkills

            val previousCustomKeys =
                state.customSkillKeys

            val mergedSkills =
                LinkedHashMap<
                        SkillCode,
                        ProfileSkillSource
                        >()

            resolved.skillCodes.forEach { code ->
                mergedSkills[
                    code
                ] =
                    if (
                        state.skills[
                            code
                        ] ==
                        ProfileSkillSource.MANUAL
                    ) {
                        ProfileSkillSource.MANUAL
                    } else {
                        ProfileSkillSource.CV
                    }
            }

            state.skills.forEach {
                    (
                        code,
                        source
                    ) ->

                if (
                    source ==
                    ProfileSkillSource.MANUAL &&
                    code !in mergedSkills
                ) {
                    mergedSkills[
                        code
                    ] =
                        ProfileSkillSource.MANUAL
                }
            }

            val manualCustoms =
                state.customSkills
                    .filter { customSkill ->
                        customSkill.source ==
                                ProfileSkillSource.MANUAL
                    }

            val manualCustomKeys =
                manualCustoms
                    .map { customSkill ->
                        customSkill.normalizedName
                    }
                    .toSet()

            val cvCustoms =
                resolved.customSkillNames
                    .filter { name ->
                        CustomSkill.normalize(
                            name
                        ) !in manualCustomKeys
                    }
                    .map { name ->
                        CustomSkill(
                            name,
                            ProfileSkillSource.CV
                        )
                    }

            val mergedCustoms =
                (
                        cvCustoms +
                                manualCustoms
                        )
                    .distinctBy { customSkill ->
                        customSkill.normalizedName
                    }

            val suggestedRole =
                cvProfile.suggestedRoles
                    .firstOrNull()
                    ?.trim()
                    .orEmpty()

            val suggestedLevel =
                cvProfile
                    .suggestedExperienceLevel
                    ?.trim()
                    .orEmpty()

            val addedCodes =
                resolved.skillCodes
                    .count { code ->
                        code !in
                                previousCodes
                    }

            val addedCustoms =
                resolved.customSkillNames
                    .count { name ->
                        CustomSkill.normalize(
                            name
                        ) !in previousCustomKeys
                    }

            state.copy(
                targetRole =
                    suggestedRole.ifBlank {
                        state.targetRole
                    },
                experienceLevel =
                    suggestedLevel.ifBlank {
                        state.experienceLevel
                    },
                skills =
                    mergedSkills,
                customSkills =
                    mergedCustoms,
                recentlyRemoved =
                    null,
                cvApplySummary =
                    CvApplySummary(
                        addedCount =
                            addedCodes +
                                    addedCustoms,
                        totalCount =
                            resolved.skillCodes.size +
                                    resolved.customSkillNames.size
                    )
            )
        }
    }

    fun dismissCvMessage() {
        mutableUiState.update { state ->
            val status =
                state.cvImport.status

            if (
                status !=
                CvImportStatus.Error &&
                status !=
                CvImportStatus.InsufficientText
            ) {
                state
            } else {
                state.copy(
                    cvImport =
                        if (
                            state.savedCvProfile !=
                            null
                        ) {
                            CvImportUiState(
                                status =
                                    CvImportStatus.Success,
                                profile =
                                    state.savedCvProfile
                            )
                        } else {
                            CvImportUiState()
                        }
                )
            }
        }
    }

    fun clearCvAnalysis() {
        cvImportJob?.cancel()

        cvImportJob =
            null

        viewModelScope.launch {
            cvAnalysisRepository
                .clearSavedProfile()
                .onSuccess {
                    mutableUiState.update { state ->
                        state.copy(
                            savedCvProfile =
                                null,
                            cvImport =
                                CvImportUiState(),
                            cvApplySummary =
                                null
                        )
                    }
                }
                .onFailure { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    mutableUiState.update { state ->
                        state.copy(
                            errorMessage =
                                throwable.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: DEFAULT_CLEAR_CV_ERROR
                        )
                    }
                }
        }
    }

    fun save() {
        val snapshot =
            mutableUiState.value

        if (!snapshot.canSave) {
            return
        }

        viewModelScope.launch {
            mutableUiState.update { state ->
                state.copy(
                    isSaving =
                        true,
                    isSaved =
                        false,
                    errorMessage =
                        null
                )
            }

            try {
                profileRepository
                    .saveProfileSnapshot(
                        targetRole =
                            snapshot.targetRole,
                        experienceLevel =
                            snapshot.experienceLevel,
                        skills =
                            snapshot.skills,
                        customSkills =
                            snapshot.customSkills
                    )

                mutableUiState.update { state ->
                    state.copy(
                        isSaving =
                            false,
                        isSaved =
                            true,
                        isDirty =
                            false,
                        recentlyRemoved =
                            null
                    )
                }
            } catch (
                cancellation:
                CancellationException
            ) {
                throw cancellation
            } catch (
                throwable: Throwable
            ) {
                mutableUiState.update { state ->
                    state.copy(
                        isSaving =
                            false,
                        isSaved =
                            false,
                        errorMessage =
                            throwable.message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: DEFAULT_SAVE_ERROR
                    )
                }
            }
        }
    }

    fun dismissError() {
        mutableUiState.update { state ->
            state.copy(
                errorMessage =
                    null
            )
        }
    }

    private fun restoreSavedCvProfile() {
        viewModelScope.launch {
            cvAnalysisRepository
                .getSavedProfile()
                .onSuccess { profile ->
                    if (profile == null) {
                        return@onSuccess
                    }

                    mutableUiState.update { state ->
                        val currentProfile =
                            state.cvImport.profile

                        state.copy(
                            savedCvProfile =
                                profile,
                            cvImport =
                                if (
                                    currentProfile !=
                                    null ||
                                    state.cvImport.isWorking
                                ) {
                                    state.cvImport
                                } else {
                                    CvImportUiState(
                                        status =
                                            CvImportStatus.Success,
                                        profile =
                                            profile
                                    )
                                }
                        )
                    }
                }
                .onFailure { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    mutableUiState.update { state ->
                        state.copy(
                            errorMessage =
                                throwable.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: DEFAULT_RESTORE_CV_ERROR
                        )
                    }
                }
        }
    }

    private suspend fun analyzeExtractedCv(
        fileName: String?,
        importResult:
        CvImportResult.Success
    ) {
        updateCvImportState(
            CvImportUiState(
                status =
                    CvImportStatus.Analyzing,
                fileName =
                    fileName,
                pageCount =
                    importResult.pageCount,
                characterCount =
                    importResult.characterCount,
                wordCount =
                    importResult.wordCount
            )
        )

        val currentTargetRole =
            mutableUiState
                .value
                .targetRole
                .trim()
                .takeIf(
                    String::isNotBlank
                )

        analyzeCvUseCase(
            cvText =
                importResult.text,
            currentTargetRole =
                currentTargetRole,
            allowedSkillCodes =
                SkillCode.entries.toSet()
        )
            .onSuccess { profile ->
                mutableUiState.update { state ->
                    state.copy(
                        savedCvProfile =
                            profile,
                        cvImport =
                            CvImportUiState(
                                status =
                                    CvImportStatus.Success,
                                fileName =
                                    fileName,
                                pageCount =
                                    importResult.pageCount,
                                characterCount =
                                    importResult.characterCount,
                                wordCount =
                                    importResult.wordCount,
                                profile =
                                    profile
                            ),
                        errorMessage =
                            null
                    )
                }
            }
            .onFailure { throwable ->
                if (
                    throwable is
                            CancellationException
                ) {
                    throw throwable
                }

                updateCvImportState(
                    CvImportUiState(
                        status =
                            CvImportStatus.Error,
                        fileName =
                            fileName,
                        pageCount =
                            importResult.pageCount,
                        characterCount =
                            importResult.characterCount,
                        wordCount =
                            importResult.wordCount,
                        errorMessage =
                            throwable.message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: DEFAULT_ANALYSIS_ERROR
                    )
                )
            }
    }

    private fun updateCvImportState(
        cvImportState: CvImportUiState
    ) {
        mutableUiState.update { state ->
            state.copy(
                cvImport =
                    cvImportState,
                errorMessage =
                    null
            )
        }
    }

    private fun resolveFileName(
        uri: Uri
    ): String? {
        return runCatching {
            context
                .contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) {
                        return@use null
                    }

                    val columnIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    if (columnIndex < 0) {
                        return@use null
                    }

                    cursor
                        .getString(
                            columnIndex
                        )
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        )
                }
        }.getOrNull()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                profileRepository
                    .observeProfile(),
                profileRepository
                    .observeCustomSkills()
            ) {
                    profile,
                    customSkills ->

                profile to
                        customSkills
            }
                .collectLatest {
                        (
                            profile,
                            customSkills
                        ) ->

                    mutableUiState.update { current ->
                        when {
                            current.isDirty -> {
                                current.copy(
                                    isLoading =
                                        false
                                )
                            }

                            profile == null -> {
                                current.copy(
                                    customSkills =
                                        customSkills,
                                    isLoading =
                                        false
                                )
                            }

                            else -> {
                                val cvSkills =
                                    profile.cvSkills
                                        .associateWith {
                                            ProfileSkillSource.CV
                                        }

                                val manualSkills =
                                    profile.manualSkills
                                        .associateWith {
                                            ProfileSkillSource.MANUAL
                                        }

                                current.copy(
                                    targetRole =
                                        profile.targetRole,
                                    experienceLevel =
                                        profile.experienceLevel,
                                    skills =
                                        cvSkills +
                                                manualSkills,
                                    customSkills =
                                        customSkills,
                                    isLoading =
                                        false
                                )
                            }
                        }
                    }
                }
        }
    }

    private inline fun markEdited(
        crossinline transform:
            (ProfileUiState) ->
        ProfileUiState
    ) {
        mutableUiState.update { state ->
            val updated =
                transform(
                    state
                )

            if (updated == state) {
                state
            } else {
                updated.copy(
                    isDirty =
                        true,
                    isSaved =
                        false,
                    errorMessage =
                        null
                )
            }
        }
    }

    override fun onCleared() {
        cvImportJob?.cancel()

        super.onCleared()
    }

    companion object {
        private const val DEFAULT_IMPORT_ERROR =
            "Sangfroid could not read the selected CV."

        private const val DEFAULT_ANALYSIS_ERROR =
            "Sangfroid could not analyze the selected CV."

        private const val DEFAULT_SAVE_ERROR =
            "The interview profile could not be saved."

        private const val DEFAULT_RESTORE_CV_ERROR =
            "The saved CV analysis could not be restored."

        private const val DEFAULT_CLEAR_CV_ERROR =
            "The saved CV analysis could not be cleared."
    }
}