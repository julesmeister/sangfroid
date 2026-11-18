package com.julesmeister.sangfroid.domain.history

import javax.inject.Inject

class DeleteSavedRevisionUseCase @Inject constructor(
    private val repository:
    PracticeHistoryRepository
) {

    suspend operator fun invoke(
        revisionId: String
    ) {
        val normalizedRevisionId =
            revisionId.trim()

        require(
            normalizedRevisionId.isNotBlank()
        ) {
            "Revision ID cannot be blank."
        }

        repository.deleteSavedRevision(
            revisionId =
                normalizedRevisionId
        )
    }
}