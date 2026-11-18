package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.Difficulty
import com.julesmeister.sangfroid.domain.model.QuestionAngle
import com.julesmeister.sangfroid.domain.model.QuestionCategory

@Entity(
    tableName = "questions",
    indices = [
        Index(
            value = [
                "category"
            ]
        ),
        Index(
            value = [
                "difficulty"
            ]
        ),
        Index(
            value = [
                "isActive"
            ]
        ),
        Index(
            value = [
                "evidenceType",
                "evidenceId"
            ]
        ),
        Index(
            value = [
                "evidenceType",
                "evidenceId",
                "questionAngle"
            ]
        ),
        Index(
            value = [
                "questionAngle"
            ]
        )
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val question: String,
    val category: QuestionCategory,
    val difficulty: Difficulty,
    val whyAsked: String,
    val answerFramework: String?,
    val estimatedMinutes: Int,
    val source: String,
    val generationReason: String? =
        null,
    val evidenceType: String? =
        null,
    val evidenceId: String? =
        null,
    val questionAngle: QuestionAngle =
        QuestionAngle.GENERAL,
    val isActive: Boolean =
        true,
    val createdAt: Long =
        System.currentTimeMillis(),
    val updatedAt: Long =
        System.currentTimeMillis()
) {

    init {
        require(id.isNotBlank()) {
            "Question ID cannot be blank."
        }

        require(question.isNotBlank()) {
            "Question text cannot be blank."
        }

        require(whyAsked.isNotBlank()) {
            "whyAsked cannot be blank."
        }

        require(estimatedMinutes in 1..30) {
            "estimatedMinutes must be between one and thirty."
        }

        require(source.isNotBlank()) {
            "Question source cannot be blank."
        }

        require(
            (evidenceType == null) ==
                    (evidenceId == null)
        ) {
            "Question evidenceType and evidenceId must both be present or both be null."
        }

        require(
            evidenceType == null ||
                    evidenceType.isNotBlank()
        ) {
            "Question evidenceType cannot be blank."
        }

        require(
            evidenceId == null ||
                    evidenceId.isNotBlank()
        ) {
            "Question evidenceId cannot be blank."
        }
    }
}