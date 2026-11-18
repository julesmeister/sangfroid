package com.julesmeister.sangfroid.data.local.model

import com.julesmeister.sangfroid.domain.model.QuestionCategory

data class CategoryInteractionRow(
    val category: QuestionCategory,
    val interactionCount: Int
) {

    init {
        require(
            interactionCount >= 0
        ) {
            "interactionCount cannot be negative."
        }
    }
}