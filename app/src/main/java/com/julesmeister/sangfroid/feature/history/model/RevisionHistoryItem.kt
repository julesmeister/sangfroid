package com.julesmeister.sangfroid.feature.history.model

import com.julesmeister.sangfroid.domain.history.RevisionSource
import com.julesmeister.sangfroid.domain.history.SavedRevision
import java.text.DateFormat
import java.util.Date

data class RevisionHistoryItem(
    val revision: SavedRevision
) {

    val id: String
        get() =
            revision.id

    val title: String
        get() =
            "Version ${revision.versionNumber}"

    val sourceLabel: String
        get() =
            when (revision.source) {
                RevisionSource.QWEN_EXAMPLE -> {
                    "Qwen example"
                }

                RevisionSource.USER_EDITED -> {
                    "Edited by you"
                }

                RevisionSource.REGENERATED -> {
                    "Regenerated example"
                }
            }

    val createdLabel: String
        get() =
            DateFormat
                .getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
                )
                .format(
                    Date(
                        revision.createdAt
                    )
                )

    val content: String
        get() =
            revision.content
                .trim()

    val preview: String
        get() =
            content
                .replace(
                    Regex("\\s+"),
                    " "
                )
}