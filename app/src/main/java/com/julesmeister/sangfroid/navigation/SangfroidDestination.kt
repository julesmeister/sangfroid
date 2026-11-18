// app/src/main/java/com/julesmeister/sangfroid/navigation/SangfroidDestination.kt

package com.julesmeister.sangfroid.navigation

import android.net.Uri

object SangfroidDestination {

    const val Feed =
        "feed"

    const val Saved =
        "saved"

    const val Profile =
        "profile"

    const val Progress =
        "progress"

    const val QuestionHistoryArgument =
        "questionId"

    const val QuestionHistory =
        "question-history/{$QuestionHistoryArgument}"

    const val PracticeArgument =
        "questionId"

    const val Practice =
        "practice/{$PracticeArgument}"

    const val FeedbackArgument =
        "attemptId"

    const val Feedback =
        "feedback/{$FeedbackArgument}"

    const val PracticeRevisionRequestKey =
        "practice_revision_request"

    const val PracticeSelectedAttemptIdKey =
        "practice_selected_attempt_id"

    const val PracticeSuggestedScriptKey =
        "practice_suggested_script"

    fun questionHistory(
        questionId: String
    ): String {
        return "question-history/${
            Uri.encode(questionId)
        }"
    }

    fun practice(
        questionId: String
    ): String {
        return "practice/${
            Uri.encode(questionId)
        }"
    }

    fun feedback(
        attemptId: String
    ): String {
        return "feedback/${
            Uri.encode(attemptId)
        }"
    }
}