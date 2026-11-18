package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionSkillCrossRef
import com.julesmeister.sangfroid.data.local.model.CvEvidenceAngleUsageRow
import com.julesmeister.sangfroid.data.local.model.CvEvidenceUsageRow
import com.julesmeister.sangfroid.data.local.model.RecommendationWeakSkillRow
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Query(
        """
        SELECT *
        FROM questions
        WHERE isActive = 1
        AND NOT EXISTS (
            SELECT 1
            FROM swipe_decisions
            WHERE swipe_decisions.questionId = questions.id
        )
        ORDER BY createdAt ASC
        """
    )
    fun observeUnswipedQuestions():
            Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT *
        FROM questions
        WHERE isActive = 1
        ORDER BY createdAt ASC
        """
    )
    fun observeAllActiveQuestions():
            Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT *
        FROM questions
        ORDER BY createdAt ASC
        """
    )
    suspend fun getAllQuestions():
            List<QuestionEntity>

    @Query(
        """
        SELECT
            evidenceType AS evidenceType,
            evidenceId AS evidenceId,
            COUNT(*) AS usageCount,
            MAX(createdAt) AS lastUsedAt
        FROM questions
        WHERE evidenceType IS NOT NULL
          AND evidenceType != ''
          AND evidenceId IS NOT NULL
          AND evidenceId != ''
        GROUP BY
            evidenceType,
            evidenceId
        ORDER BY
            usageCount ASC,
            lastUsedAt ASC,
            evidenceType ASC,
            evidenceId ASC
        """
    )
    suspend fun getCvEvidenceUsage():
            List<CvEvidenceUsageRow>

    @Query(
        """
        SELECT
            evidenceType AS evidenceType,
            evidenceId AS evidenceId,
            questionAngle AS questionAngle,
            COUNT(*) AS usageCount,
            MAX(createdAt) AS lastUsedAt
        FROM questions
        WHERE evidenceType IS NOT NULL
          AND evidenceType != ''
          AND evidenceId IS NOT NULL
          AND evidenceId != ''
        GROUP BY
            evidenceType,
            evidenceId,
            questionAngle
        ORDER BY
            usageCount ASC,
            lastUsedAt ASC,
            evidenceType ASC,
            evidenceId ASC,
            questionAngle ASC
        """
    )
    suspend fun getCvEvidenceAngleUsage():
            List<CvEvidenceAngleUsageRow>

    @Query(
        """
        SELECT *
        FROM questions
        WHERE isActive = 1
        AND category = :category
        AND NOT EXISTS (
            SELECT 1
            FROM swipe_decisions
            WHERE swipe_decisions.questionId = questions.id
        )
        ORDER BY createdAt ASC
        """
    )
    fun observeUnswipedQuestionsByCategory(
        category: QuestionCategory
    ): Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT *
        FROM questions
        WHERE id = :questionId
        LIMIT 1
        """
    )
    fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?>

    @Query(
        """
        SELECT questions.*
        FROM questions
        INNER JOIN saved_questions
            ON saved_questions.questionId = questions.id
        WHERE questions.isActive = 1
        ORDER BY saved_questions.savedAt DESC
        """
    )
    fun observeSavedQuestions():
            Flow<List<QuestionEntity>>

    @Query(
        """
        SELECT *
        FROM question_skills
        WHERE questionId IN (:questionIds)
        """
    )
    suspend fun getQuestionSkills(
        questionIds: List<String>
    ): List<QuestionSkillCrossRef>

    @Query(
        """
        SELECT skillCode
        FROM question_skills
        WHERE questionId = :questionId
        """
    )
    suspend fun getSkillCodesForQuestion(
        questionId: String
    ): List<SkillCode>

    @Query(
        """
        SELECT COUNT(*)
        FROM questions
        """
    )
    suspend fun countQuestions(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM questions
        WHERE isActive = 1
        AND NOT EXISTS (
            SELECT 1
            FROM swipe_decisions
            WHERE swipe_decisions.questionId = questions.id
        )
        """
    )
    suspend fun countAvailableQuestions(): Int

    @Query(
        """
        SELECT questionId
        FROM swipe_decisions
        ORDER BY swipedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentQuestionIds(
        limit: Int
    ): List<String>

    @Query(
        """
        SELECT DISTINCT question_skills.skillCode
        FROM question_skills
        INNER JOIN saved_questions
            ON saved_questions.questionId =
               question_skills.questionId
        ORDER BY saved_questions.savedAt DESC
        LIMIT :limit
        """
    )
    suspend fun getSavedSkillCodes(
        limit: Int
    ): List<SkillCode>

    @Query(
        """
        SELECT
            question_skills.skillCode AS skillCode,
            AVG(
                CASE
                    WHEN rubric_scores.maximumScore > 0
                    THEN (
                        CAST(rubric_scores.score AS REAL)
                        /
                        CAST(rubric_scores.maximumScore AS REAL)
                    ) * 5.0
                    ELSE 0.0
                END
            ) AS averageScore,
            COUNT(rubric_scores.id) AS evidenceCount
        FROM rubric_scores
        INNER JOIN answer_evaluations
            ON answer_evaluations.id =
               rubric_scores.evaluationId
        INNER JOIN practice_attempts
            ON practice_attempts.id =
               answer_evaluations.attemptId
        INNER JOIN question_skills
            ON question_skills.questionId =
               practice_attempts.questionId
        GROUP BY question_skills.skillCode
        HAVING COUNT(rubric_scores.id) > 0
        ORDER BY averageScore ASC,
                 evidenceCount DESC
        LIMIT :limit
        """
    )
    suspend fun getWeakSkillRows(
        limit: Int
    ): List<RecommendationWeakSkillRow>

    @Query(
        """
        SELECT answer_evaluations.missingPoints
        FROM answer_evaluations
        INNER JOIN practice_attempts
            ON practice_attempts.id =
               answer_evaluations.attemptId
        INNER JOIN question_skills
            ON question_skills.questionId =
               practice_attempts.questionId
        WHERE question_skills.skillCode =
              :skillCode
        AND answer_evaluations.missingPoints != ''
        ORDER BY answer_evaluations.createdAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentMissingPointsForSkill(
        skillCode: SkillCode,
        limit: Int
    ): List<String>

    @Query(
        """
        SELECT questions.category
        FROM questions
        INNER JOIN saved_questions
            ON saved_questions.questionId =
               questions.id
        GROUP BY questions.category
        ORDER BY COUNT(saved_questions.id) DESC
        LIMIT :limit
        """
    )
    suspend fun getPreferredCategories(
        limit: Int
    ): List<QuestionCategory>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertQuestions(
        questions: List<QuestionEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertQuestionSkills(
        skills: List<QuestionSkillCrossRef>
    )

    @Transaction
    suspend fun insertQuestionCatalogue(
        questions: List<QuestionEntity>,
        skills: List<QuestionSkillCrossRef>
    ) {
        if (questions.isEmpty()) {
            return
        }

        insertQuestions(questions)

        if (skills.isNotEmpty()) {
            insertQuestionSkills(skills)
        }
    }
}