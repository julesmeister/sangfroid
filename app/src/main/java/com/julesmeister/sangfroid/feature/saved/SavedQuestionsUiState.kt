package com.julesmeister.sangfroid.feature.saved

import com.julesmeister.sangfroid.feature.saved.model.SavedQuestionItem

data class SavedQuestionsUiState(
    val isLoading: Boolean = true,
    val questions:
    List<SavedQuestionItem> =
        emptyList(),
    val removingQuestionIds:
    Set<String> =
        emptySet(),
    val errorMessage: String? = null
) {

    val savedCount: Int
        get() =
            questions.size

    val isEmpty: Boolean
        get() =
            !isLoading &&
                    questions.isEmpty()

    fun isRemoving(
        questionId: String
    ): Boolean {
        return questionId in
                removingQuestionIds
    }
}