// app/src/main/java/com/julesmeister/sangfroid/data/local/database/SangfroidMigrations.kt

package com.julesmeister.sangfroid.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SangfroidMigrations {

    val MIGRATION_1_2 =
        object : Migration(
            1,
            2
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS candidate_profiles (
                        id TEXT NOT NULL,
                        targetRole TEXT NOT NULL,
                        experienceLevel TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS profile_skills (
                        profileId TEXT NOT NULL,
                        skillCode TEXT NOT NULL,
                        source TEXT NOT NULL,
                        PRIMARY KEY(
                            profileId,
                            skillCode,
                            source
                        ),
                        FOREIGN KEY(profileId)
                            REFERENCES candidate_profiles(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_profile_skills_profileId
                    ON profile_skills(profileId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_profile_skills_skillCode
                    ON profile_skills(skillCode)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_profile_skills_source
                    ON profile_skills(source)
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_2_3 =
        object : Migration(
            2,
            3
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_example_revisions (
                        id TEXT NOT NULL,
                        questionId TEXT NOT NULL,
                        attemptId TEXT NOT NULL,
                        evaluationId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        version INTEGER NOT NULL,
                        modelName TEXT NOT NULL,
                        modelVersion TEXT,
                        savedAt INTEGER NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(questionId)
                            REFERENCES questions(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(attemptId)
                            REFERENCES practice_attempts(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(evaluationId)
                            REFERENCES answer_evaluations(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_example_revisions_questionId
                    ON saved_example_revisions(questionId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_example_revisions_attemptId
                    ON saved_example_revisions(attemptId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_saved_example_revisions_evaluationId
                    ON saved_example_revisions(evaluationId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_saved_example_revisions_questionId_version
                    ON saved_example_revisions(
                        questionId,
                        version
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_example_revisions_savedAt
                    ON saved_example_revisions(savedAt)
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_3_4 =
        object : Migration(
            3,
            4
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_revisions (
                        id TEXT NOT NULL,
                        attemptId TEXT NOT NULL,
                        questionId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        versionNumber INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(attemptId)
                            REFERENCES practice_attempts(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO saved_revisions (
                        id,
                        attemptId,
                        questionId,
                        content,
                        versionNumber,
                        source,
                        createdAt
                    )
                    SELECT
                        id,
                        attemptId,
                        questionId,
                        content,
                        ROW_NUMBER() OVER (
                            PARTITION BY questionId
                            ORDER BY
                                savedAt ASC,
                                version ASC,
                                id ASC
                        ),
                        'QWEN_EXAMPLE',
                        savedAt
                    FROM saved_example_revisions
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    DROP TABLE saved_example_revisions
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_revisions_attemptId
                    ON saved_revisions(attemptId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_revisions_questionId
                    ON saved_revisions(questionId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_saved_revisions_questionId_createdAt
                    ON saved_revisions(
                        questionId,
                        createdAt
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_saved_revisions_questionId_versionNumber
                    ON saved_revisions(
                        questionId,
                        versionNumber
                    )
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_4_5 =
        object : Migration(
            4,
            5
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_profiles (
                        profile_id INTEGER NOT NULL,
                        profile_summary TEXT NOT NULL,
                        suggested_roles_json TEXT NOT NULL,
                        suggested_experience_level TEXT,
                        experience_signals_json TEXT NOT NULL,
                        model_name TEXT NOT NULL,
                        model_version TEXT,
                        analyzed_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        PRIMARY KEY(profile_id)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_skills (
                        skill_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profile_owner_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        normalized_name TEXT NOT NULL,
                        skill_type TEXT NOT NULL,
                        confidence REAL NOT NULL,
                        evidence TEXT NOT NULL,
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_skills_profile_owner_id
                    ON cv_skills(profile_owner_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_cv_skills_profile_owner_id_normalized_name
                    ON cv_skills(
                        profile_owner_id,
                        normalized_name
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_skill_mappings (
                        mapping_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profile_owner_id INTEGER NOT NULL,
                        extracted_skill_name TEXT NOT NULL,
                        skill_code TEXT NOT NULL,
                        confidence REAL NOT NULL,
                        reason TEXT NOT NULL,
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_skill_mappings_profile_owner_id
                    ON cv_skill_mappings(profile_owner_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_cv_skill_mappings_profile_owner_id_extracted_skill_name_skill_code
                    ON cv_skill_mappings(
                        profile_owner_id,
                        extracted_skill_name,
                        skill_code
                    )
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_5_6 =
        object : Migration(
            5,
            6
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_profile_skills (
                        profileId TEXT NOT NULL,
                        normalizedName TEXT NOT NULL,
                        name TEXT NOT NULL,
                        source TEXT NOT NULL,
                        PRIMARY KEY(
                            profileId,
                            normalizedName
                        ),
                        FOREIGN KEY(profileId)
                            REFERENCES candidate_profiles(id)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_custom_profile_skills_profileId
                    ON custom_profile_skills(profileId)
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_6_7 =
        object : Migration(
            6,
            7
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_certifications (
                        profile_owner_id INTEGER NOT NULL,
                        evidence_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        issuer TEXT,
                        level TEXT,
                        status TEXT NOT NULL,
                        issued_year INTEGER,
                        evidence TEXT NOT NULL,
                        related_skills_json TEXT NOT NULL,
                        PRIMARY KEY(
                            profile_owner_id,
                            evidence_id
                        ),
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_certifications_profile_owner_id
                    ON cv_certifications(profile_owner_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_projects (
                        profile_owner_id INTEGER NOT NULL,
                        evidence_id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        context TEXT,
                        candidate_role TEXT,
                        objective TEXT,
                        actions_json TEXT NOT NULL,
                        methods_json TEXT NOT NULL,
                        tools_json TEXT NOT NULL,
                        outcomes_json TEXT NOT NULL,
                        metrics_json TEXT NOT NULL,
                        related_skills_json TEXT NOT NULL,
                        evidence TEXT NOT NULL,
                        PRIMARY KEY(
                            profile_owner_id,
                            evidence_id
                        ),
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_projects_profile_owner_id
                    ON cv_projects(profile_owner_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_achievements (
                        profile_owner_id INTEGER NOT NULL,
                        evidence_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        context TEXT,
                        candidate_action TEXT,
                        result TEXT,
                        metric TEXT,
                        evidence TEXT NOT NULL,
                        related_skills_json TEXT NOT NULL,
                        PRIMARY KEY(
                            profile_owner_id,
                            evidence_id
                        ),
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_achievements_profile_owner_id
                    ON cv_achievements(profile_owner_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cv_work_highlights (
                        profile_owner_id INTEGER NOT NULL,
                        evidence_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        organization_context TEXT,
                        candidate_role TEXT,
                        responsibility TEXT,
                        action TEXT,
                        outcome TEXT,
                        metric TEXT,
                        evidence TEXT NOT NULL,
                        related_skills_json TEXT NOT NULL,
                        PRIMARY KEY(
                            profile_owner_id,
                            evidence_id
                        ),
                        FOREIGN KEY(profile_owner_id)
                            REFERENCES cv_profiles(profile_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_cv_work_highlights_profile_owner_id
                    ON cv_work_highlights(profile_owner_id)
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_7_8 =
        object : Migration(
            7,
            8
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE questions
                    ADD COLUMN generationReason TEXT
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE questions
                    ADD COLUMN evidenceType TEXT
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE questions
                    ADD COLUMN evidenceId TEXT
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_questions_evidenceType_evidenceId
                    ON questions(
                        evidenceType,
                        evidenceId
                    )
                    """.trimIndent()
                )
            }
        }

    val MIGRATION_8_9 =
        object : Migration(
            8,
            9
        ) {
            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                ALTER TABLE questions
                ADD COLUMN questionAngle TEXT NOT NULL
                DEFAULT 'GENERAL'
                """.trimIndent()
                )

                database.execSQL(
                    """
                CREATE INDEX IF NOT EXISTS
                index_questions_questionAngle
                ON questions(questionAngle)
                """.trimIndent()
                )

                database.execSQL(
                    """
                CREATE INDEX IF NOT EXISTS
                index_questions_evidenceType_evidenceId_questionAngle
                ON questions(
                    evidenceType,
                    evidenceId,
                    questionAngle
                )
                """.trimIndent()
                )
            }
        }
}