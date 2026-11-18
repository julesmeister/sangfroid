package com.julesmeister.sangfroid.feature.feed.custom

import com.julesmeister.sangfroid.domain.repository.QuestionImportRepository

data class QuestionImportUiState(
    val isVisible: Boolean = false,
    val pastedContent: String = "",
    val stage: QuestionImportStage =
        QuestionImportStage.Editing,
    val preview:
    QuestionImportRepository.ImportPreview? =
        null,
    val lastImportResult:
    QuestionImportRepository.ImportResult? =
        null,
    val errorMessage: String? = null
) {

    val canAnalyze: Boolean
        get() =
            stage ==
                    QuestionImportStage.Editing &&
                    pastedContent
                        .trim()
                        .length >=
                    MINIMUM_CONTENT_LENGTH

    val canImport: Boolean
        get() =
            stage ==
                    QuestionImportStage.Preview &&
                    preview
                        ?.importableCount
                        ?.let { count ->
                            count > 0
                        } ==
                    true

    val isBusy: Boolean
        get() =
            stage ==
                    QuestionImportStage.Extracting ||
                    stage ==
                    QuestionImportStage.Importing

    val contentCharacterCount: Int
        get() =
            pastedContent.length

    companion object {
        const val MAXIMUM_CONTENT_LENGTH =
            50_000

        private const val MINIMUM_CONTENT_LENGTH =
            20
    }
}

enum class QuestionImportStage {
    Editing,
    Extracting,
    Preview,
    Importing,
    Complete
}