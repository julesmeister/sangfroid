package com.julesmeister.sangfroid.domain.history

data class SavedRevision(
    val id: String,
    val attemptId: String,
    val questionId: String,
    val content: String,
    val versionNumber: Int,
    val source: RevisionSource,
    val createdAt: Long
)

enum class RevisionSource {
    QWEN_EXAMPLE,
    USER_EDITED,
    REGENERATED;

    companion object {

        fun fromStorageValue(
            value: String
        ): RevisionSource {
            return entries.firstOrNull { source ->
                source.name.equals(
                    other = value,
                    ignoreCase = true
                )
            } ?: QWEN_EXAMPLE
        }
    }
}