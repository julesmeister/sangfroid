package com.julesmeister.sangfroid.data.local.converter

import androidx.room.TypeConverter
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.Difficulty
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import com.julesmeister.sangfroid.domain.model.QuestionAngle
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode

class SangfroidConverters {

    @TypeConverter
    fun questionCategoryToString(
        value: QuestionCategory
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToQuestionCategory(
        value: String
    ): QuestionCategory {
        return QuestionCategory.valueOf(
            value
        )
    }

    @TypeConverter
    fun difficultyToString(
        value: Difficulty
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToDifficulty(
        value: String
    ): Difficulty {
        return Difficulty.valueOf(
            value
        )
    }

    @TypeConverter
    fun skillCodeToString(
        value: SkillCode
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToSkillCode(
        value: String
    ): SkillCode {
        return SkillCode.valueOf(
            value
        )
    }

    @TypeConverter
    fun answerInputMethodToString(
        value: AnswerInputMethod
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToAnswerInputMethod(
        value: String
    ): AnswerInputMethod {
        return AnswerInputMethod.valueOf(
            value
        )
    }

    @TypeConverter
    fun evaluationStatusToString(
        value: EvaluationStatus
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToEvaluationStatus(
        value: String
    ): EvaluationStatus {
        return EvaluationStatus.valueOf(
            value
        )
    }

    @TypeConverter
    fun questionAngleToString(
        value: QuestionAngle
    ): String {
        return value.name
    }

    @TypeConverter
    fun stringToQuestionAngle(
        value: String
    ): QuestionAngle {
        return runCatching {
            QuestionAngle.valueOf(
                value
            )
        }.getOrDefault(
            QuestionAngle.GENERAL
        )
    }
}