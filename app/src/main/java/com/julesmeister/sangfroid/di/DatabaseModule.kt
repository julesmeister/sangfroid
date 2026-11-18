package com.julesmeister.sangfroid.di

import android.content.Context
import androidx.room.Room
import com.julesmeister.sangfroid.data.cv.CvDocumentReader
import com.julesmeister.sangfroid.data.cv.OfflineCvSkillExtractor
import com.julesmeister.sangfroid.data.cv.PdfCvDocumentReader
import com.julesmeister.sangfroid.data.cv.PdfCvTextExtractor
import com.julesmeister.sangfroid.data.cv.RoomCvAnalysisRepository
import com.julesmeister.sangfroid.data.evaluation.QwenEvaluationEngine
import com.julesmeister.sangfroid.data.history.RoomPracticeHistoryRepository
import com.julesmeister.sangfroid.data.local.dao.CvProfileDao
import com.julesmeister.sangfroid.data.local.dao.EvaluationDao
import com.julesmeister.sangfroid.data.local.dao.PracticeDao
import com.julesmeister.sangfroid.data.local.dao.ProfileDao
import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.dao.QuestionInteractionDao
import com.julesmeister.sangfroid.data.local.dao.SavedRevisionDao
import com.julesmeister.sangfroid.data.local.database.SangfroidDatabase
import com.julesmeister.sangfroid.data.local.database.SangfroidMigrations
import com.julesmeister.sangfroid.data.progress.RoomQuestionProgressRepository
import com.julesmeister.sangfroid.data.remote.api.HttpSangfroidApi
import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.auth.AnonymousTokenProvider
import com.julesmeister.sangfroid.data.remote.auth.AuthTokenProvider
import com.julesmeister.sangfroid.data.repository.OfflineFirstEvaluationRepository
import com.julesmeister.sangfroid.data.repository.OfflineFirstQuestionGenerationRepository
import com.julesmeister.sangfroid.data.repository.OfflineFirstQuestionImportRepository
import com.julesmeister.sangfroid.data.repository.OfflinePracticeRepository
import com.julesmeister.sangfroid.data.repository.OfflineProfileRepository
import com.julesmeister.sangfroid.data.repository.OfflineQuestionRepository
import com.julesmeister.sangfroid.data.speech.SherpaOnlineStreamingEngineFactory
import com.julesmeister.sangfroid.data.speech.SherpaSpeechRecognizer
import com.julesmeister.sangfroid.data.speech.SherpaStreamingEngineFactory
import com.julesmeister.sangfroid.data.speech.SherpaWhisperFinalSpeechRecognizer
import com.julesmeister.sangfroid.domain.cv.CvAnalysisRepository
import com.julesmeister.sangfroid.domain.cv.CvSkillExtractor
import com.julesmeister.sangfroid.domain.cv.CvTextExtractor
import com.julesmeister.sangfroid.domain.evaluation.AnswerEvaluationEngine
import com.julesmeister.sangfroid.domain.history.PracticeHistoryRepository
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.domain.repository.EvaluationRepository
import com.julesmeister.sangfroid.domain.repository.PracticeRepository
import com.julesmeister.sangfroid.domain.repository.ProfileRepository
import com.julesmeister.sangfroid.domain.repository.QuestionGenerationRepository
import com.julesmeister.sangfroid.domain.repository.QuestionImportRepository
import com.julesmeister.sangfroid.domain.repository.QuestionRepository
import com.julesmeister.sangfroid.domain.speech.FinalSpeechRecognizer
import com.julesmeister.sangfroid.domain.speech.SpeechRecognizer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object DatabaseProviderModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext
        context: Context
    ): SangfroidDatabase {
        return Room.databaseBuilder(
            context,
            SangfroidDatabase::class.java,
            SangfroidDatabase.DATABASE_NAME
        )
            .addMigrations(
                SangfroidMigrations.MIGRATION_1_2,
                SangfroidMigrations.MIGRATION_2_3,
                SangfroidMigrations.MIGRATION_3_4,
                SangfroidMigrations.MIGRATION_4_5,
                SangfroidMigrations.MIGRATION_5_6,
                SangfroidMigrations.MIGRATION_6_7,
                SangfroidMigrations.MIGRATION_7_8,
                SangfroidMigrations.MIGRATION_8_9
            )
            .build()
    }

    @Provides
    fun provideQuestionDao(
        database: SangfroidDatabase
    ): QuestionDao {
        return database.questionDao()
    }

    @Provides
    fun provideQuestionInteractionDao(
        database: SangfroidDatabase
    ): QuestionInteractionDao {
        return database
            .questionInteractionDao()
    }

    @Provides
    fun providePracticeDao(
        database: SangfroidDatabase
    ): PracticeDao {
        return database.practiceDao()
    }

    @Provides
    fun provideEvaluationDao(
        database: SangfroidDatabase
    ): EvaluationDao {
        return database.evaluationDao()
    }

    @Provides
    fun provideProfileDao(
        database: SangfroidDatabase
    ): ProfileDao {
        return database.profileDao()
    }

    @Provides
    fun provideSavedRevisionDao(
        database: SangfroidDatabase
    ): SavedRevisionDao {
        return database.savedRevisionDao()
    }

    @Provides
    fun provideCvProfileDao(
        database: SangfroidDatabase
    ): CvProfileDao {
        return database.cvProfileDao()
    }
}

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class RepositoryBindingModule {

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        implementation:
        OfflineQuestionRepository
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindPracticeRepository(
        implementation:
        OfflinePracticeRepository
    ): PracticeRepository

    @Binds
    @Singleton
    abstract fun bindEvaluationRepository(
        implementation:
        OfflineFirstEvaluationRepository
    ): EvaluationRepository

    @Binds
    @Singleton
    abstract fun bindQuestionGenerationRepository(
        implementation:
        OfflineFirstQuestionGenerationRepository
    ): QuestionGenerationRepository

    @Binds
    @Singleton
    abstract fun bindQuestionImportRepository(
        implementation:
        OfflineFirstQuestionImportRepository
    ): QuestionImportRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation:
        OfflineProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindPracticeHistoryRepository(
        implementation:
        RoomPracticeHistoryRepository
    ): PracticeHistoryRepository

    @Binds
    @Singleton
    abstract fun bindQuestionProgressRepository(
        implementation:
        RoomQuestionProgressRepository
    ): QuestionProgressRepository

    @Binds
    @Singleton
    abstract fun bindCvAnalysisRepository(
        implementation:
        RoomCvAnalysisRepository
    ): CvAnalysisRepository

    @Binds
    @Singleton
    abstract fun bindCvTextExtractor(
        implementation:
        PdfCvTextExtractor
    ): CvTextExtractor

    @Binds
    @Singleton
    abstract fun bindCvSkillExtractor(
        implementation:
        OfflineCvSkillExtractor
    ): CvSkillExtractor

    @Binds
    @Singleton
    abstract fun bindCvDocumentReader(
        implementation:
        PdfCvDocumentReader
    ): CvDocumentReader

    @Binds
    @Singleton
    abstract fun bindSpeechRecognizer(
        implementation:
        SherpaSpeechRecognizer
    ): SpeechRecognizer

    @Binds
    @Singleton
    abstract fun bindSherpaStreamingEngineFactory(
        implementation:
        SherpaOnlineStreamingEngineFactory
    ): SherpaStreamingEngineFactory

    @Binds
    @Singleton
    abstract fun bindFinalSpeechRecognizer(
        implementation:
        SherpaWhisperFinalSpeechRecognizer
    ): FinalSpeechRecognizer

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(
        implementation:
        AnonymousTokenProvider
    ): AuthTokenProvider

    @Binds
    @Singleton
    abstract fun bindSangfroidApi(
        implementation:
        HttpSangfroidApi
    ): SangfroidApi

    @Binds
    @Singleton
    abstract fun bindAnswerEvaluationEngine(
        implementation:
        QwenEvaluationEngine
    ): AnswerEvaluationEngine
}