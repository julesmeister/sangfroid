package com.julesmeister.sangfroid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.julesmeister.sangfroid.data.local.converter.SangfroidConverters
import com.julesmeister.sangfroid.data.local.dao.CvProfileDao
import com.julesmeister.sangfroid.data.local.dao.EvaluationDao
import com.julesmeister.sangfroid.data.local.dao.PracticeDao
import com.julesmeister.sangfroid.data.local.dao.ProfileDao
import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.dao.QuestionInteractionDao
import com.julesmeister.sangfroid.data.local.dao.SavedRevisionDao
import com.julesmeister.sangfroid.data.local.entity.AnswerDraftEntity
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.CandidateProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CustomProfileSkillEntity
import com.julesmeister.sangfroid.data.local.entity.CvAchievementEntity
import com.julesmeister.sangfroid.data.local.entity.CvCertificationEntity
import com.julesmeister.sangfroid.data.local.entity.CvProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CvProjectEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillMappingEntity
import com.julesmeister.sangfroid.data.local.entity.CvWorkHighlightEntity
import com.julesmeister.sangfroid.data.local.entity.DownloadedModelEntity
import com.julesmeister.sangfroid.data.local.entity.ExtractedSkillEntity
import com.julesmeister.sangfroid.data.local.entity.FollowUpQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.ProfileSkillEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionSkillCrossRef
import com.julesmeister.sangfroid.data.local.entity.ReminderEntity
import com.julesmeister.sangfroid.data.local.entity.RubricScoreEntity
import com.julesmeister.sangfroid.data.local.entity.SavedQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.SavedRevisionEntity
import com.julesmeister.sangfroid.data.local.entity.SwipeDecisionEntity
import com.julesmeister.sangfroid.data.local.entity.SyncQueueEntity
import com.julesmeister.sangfroid.data.local.entity.TranscriptEntity

@Database(
    entities = [
        QuestionEntity::class,
        QuestionSkillCrossRef::class,
        SwipeDecisionEntity::class,
        SavedQuestionEntity::class,
        AnswerDraftEntity::class,
        PracticeAttemptEntity::class,
        TranscriptEntity::class,
        AnswerEvaluationEntity::class,
        RubricScoreEntity::class,
        FollowUpQuestionEntity::class,
        ExtractedSkillEntity::class,
        ReminderEntity::class,
        DownloadedModelEntity::class,
        SyncQueueEntity::class,
        CandidateProfileEntity::class,
        ProfileSkillEntity::class,
        SavedRevisionEntity::class,
        CvProfileEntity::class,
        CvSkillEntity::class,
        CvSkillMappingEntity::class,
        CvCertificationEntity::class,
        CvProjectEntity::class,
        CvAchievementEntity::class,
        CvWorkHighlightEntity::class,
        CustomProfileSkillEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(
    SangfroidConverters::class
)
abstract class SangfroidDatabase :
    RoomDatabase() {

    abstract fun questionDao():
            QuestionDao

    abstract fun questionInteractionDao():
            QuestionInteractionDao

    abstract fun practiceDao():
            PracticeDao

    abstract fun evaluationDao():
            EvaluationDao

    abstract fun profileDao():
            ProfileDao

    abstract fun savedRevisionDao():
            SavedRevisionDao

    abstract fun cvProfileDao():
            CvProfileDao

    companion object {
        const val DATABASE_NAME =
            "sangfroid.db"
    }
}