// feature/practice/PracticeAttemptActionsViewModel.kt

package com.julesmeister.sangfroid.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.domain.history.PracticeHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeAttemptActionsState(
    val deletingAttemptIds: Set<String> = emptySet(),
    val deletedAttemptId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PracticeAttemptActionsViewModel @Inject constructor(
    private val historyRepository: PracticeHistoryRepository
) : ViewModel() {

    private val mutableState =
        MutableStateFlow(PracticeAttemptActionsState())

    val state: StateFlow<PracticeAttemptActionsState> =
        mutableState.asStateFlow()

    fun deleteAttempt(attemptId: String) {
        if (
            attemptId.isBlank() ||
            attemptId in mutableState.value.deletingAttemptIds
        ) {
            return
        }

        mutableState.update {
            it.copy(
                deletingAttemptIds =
                    it.deletingAttemptIds + attemptId,
                deletedAttemptId = null,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                historyRepository.deletePracticeAttempt(attemptId)

                mutableState.update {
                    it.copy(
                        deletingAttemptIds =
                            it.deletingAttemptIds - attemptId,
                        deletedAttemptId = attemptId,
                        errorMessage = null
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.update {
                    it.copy(
                        deletingAttemptIds =
                            it.deletingAttemptIds - attemptId,
                        errorMessage =
                            exception.message
                                ?: "This attempt could not be deleted. Please try again."
                    )
                }
            }
        }
    }

    fun consumeDeletion() {
        mutableState.update {
            it.copy(deletedAttemptId = null)
        }
    }

    fun dismissError() {
        mutableState.update {
            it.copy(errorMessage = null)
        }
    }
}