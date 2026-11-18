package com.julesmeister.sangfroid.feature.profile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.cv.CvDocumentReader
import com.julesmeister.sangfroid.data.cv.CvImportResult
import com.julesmeister.sangfroid.domain.cv.AnalyzeCvUseCase
import com.julesmeister.sangfroid.domain.model.SkillCode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CvImportViewModel @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val cvDocumentReader: CvDocumentReader,
    private val analyzeCvUseCase: AnalyzeCvUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CvImportUiState()
        )

    val uiState:
            StateFlow<CvImportUiState> =
        _uiState.asStateFlow()

    private var importJob:
            Job? =
        null

    fun importCv(
        uri: Uri,
        currentTargetRole: String? = null
    ) {
        if (_uiState.value.isWorking) {
            return
        }

        importJob?.cancel()

        importJob =
            viewModelScope.launch {
                val fileName =
                    resolveFileName(
                        uri
                    )

                _uiState.value =
                    CvImportUiState(
                        status =
                            CvImportStatus.ReadingPdf,
                        fileName =
                            fileName
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
                                currentTargetRole =
                                    currentTargetRole,
                                result =
                                    importResult
                            )
                        }

                        is CvImportResult.InsufficientText -> {
                            _uiState.value =
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
                        }

                        is CvImportResult.Failure -> {
                            _uiState.value =
                                CvImportUiState(
                                    status =
                                        CvImportStatus.Error,
                                    fileName =
                                        fileName,
                                    errorMessage =
                                        importResult.message
                                )
                        }
                    }
                } catch (
                    cancellation: CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    _uiState.value =
                        CvImportUiState(
                            status =
                                CvImportStatus.Error,
                            fileName =
                                fileName,
                            errorMessage =
                                throwable
                                    .message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: DEFAULT_IMPORT_ERROR
                        )
                }
            }
    }

    fun clearError() {
        val current =
            _uiState.value

        if (
            current.status !=
            CvImportStatus.Error &&
            current.status !=
            CvImportStatus.InsufficientText
        ) {
            return
        }

        _uiState.value =
            current.copy(
                status =
                    if (
                        current.profile !=
                        null
                    ) {
                        CvImportStatus.Success
                    } else {
                        CvImportStatus.Idle
                    },
                errorMessage =
                    null
            )
    }

    fun reset() {
        importJob?.cancel()

        importJob =
            null

        _uiState.value =
            CvImportUiState()
    }

    private suspend fun analyzeExtractedCv(
        fileName: String?,
        currentTargetRole: String?,
        result: CvImportResult.Success
    ) {
        _uiState.value =
            CvImportUiState(
                status =
                    CvImportStatus.Analyzing,
                fileName =
                    fileName,
                pageCount =
                    result.pageCount,
                characterCount =
                    result.characterCount,
                wordCount =
                    result.wordCount
            )

        val analysisResult =
            analyzeCvUseCase(
                cvText =
                    result.text,
                currentTargetRole =
                    normalizeTargetRole(
                        currentTargetRole
                    ),
                allowedSkillCodes =
                    ALLOWED_SKILL_CODES
            )

        analysisResult
            .onSuccess { profile ->
                _uiState.value =
                    CvImportUiState(
                        status =
                            CvImportStatus.Success,
                        fileName =
                            fileName,
                        pageCount =
                            result.pageCount,
                        characterCount =
                            result.characterCount,
                        wordCount =
                            result.wordCount,
                        profile =
                            profile
                    )
            }
            .onFailure { throwable ->
                if (
                    throwable is
                            CancellationException
                ) {
                    throw throwable
                }

                _uiState.value =
                    CvImportUiState(
                        status =
                            CvImportStatus.Error,
                        fileName =
                            fileName,
                        pageCount =
                            result.pageCount,
                        characterCount =
                            result.characterCount,
                        wordCount =
                            result.wordCount,
                        errorMessage =
                            throwable
                                .message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: DEFAULT_ANALYSIS_ERROR
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
                    if (
                        !cursor.moveToFirst()
                    ) {
                        return@use null
                    }

                    val columnIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    if (
                        columnIndex < 0
                    ) {
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

    private fun normalizeTargetRole(
        value: String?
    ): String? {
        return value
            ?.replace(
                "\u0000",
                ""
            )
            ?.replace(
                WHITESPACE_REGEX,
                " "
            )
            ?.trim()
            ?.take(
                MAXIMUM_TARGET_ROLE_LENGTH
            )
            ?.takeIf(
                String::isNotBlank
            )
    }

    companion object {

        /*
         * Keep SkillCode strongly typed until the remote repository.
         *
         * RemoteCvAnalysisRepository is responsible for serializing
         * SkillCode.name into the List<String> expected by Spring Boot.
         */
        private val ALLOWED_SKILL_CODES:
                Set<SkillCode> =
            SkillCode.entries.toSet()

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            150

        private const val DEFAULT_IMPORT_ERROR =
            "Sangfroid could not read the selected CV."

        private const val DEFAULT_ANALYSIS_ERROR =
            "Sangfroid could not analyze the selected CV."

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}