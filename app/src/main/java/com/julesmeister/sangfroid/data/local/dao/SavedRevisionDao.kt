package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.SavedRevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRevisionDao {

    @Insert(
        onConflict =
            OnConflictStrategy.ABORT
    )
    suspend fun insertRevision(
        revision: SavedRevisionEntity
    )

    @Query(
        """
        SELECT *
        FROM saved_revisions
        WHERE id = :revisionId
        LIMIT 1
        """
    )
    suspend fun getRevisionById(
        revisionId: String
    ): SavedRevisionEntity?

    @Query(
        """
        SELECT *
        FROM saved_revisions
        WHERE id = :revisionId
        LIMIT 1
        """
    )
    fun observeRevisionById(
        revisionId: String
    ): Flow<SavedRevisionEntity?>

    @Query(
        """
        SELECT *
        FROM saved_revisions
        WHERE attemptId = :attemptId
        ORDER BY versionNumber DESC, createdAt DESC
        """
    )
    fun observeRevisionsForAttempt(
        attemptId: String
    ): Flow<List<SavedRevisionEntity>>

    @Query(
        """
        SELECT *
        FROM saved_revisions
        WHERE questionId = :questionId
        ORDER BY versionNumber DESC, createdAt DESC
        """
    )
    fun observeRevisionsForQuestion(
        questionId: String
    ): Flow<List<SavedRevisionEntity>>

    @Query(
        """
        SELECT COALESCE(
            MAX(versionNumber),
            0
        )
        FROM saved_revisions
        WHERE questionId = :questionId
        """
    )
    suspend fun getLatestVersionNumberForQuestion(
        questionId: String
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM saved_revisions
        WHERE attemptId = :attemptId
        """
    )
    suspend fun getRevisionCountForAttempt(
        attemptId: String
    ): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM saved_revisions
        WHERE questionId = :questionId
        """
    )
    suspend fun getRevisionCountForQuestion(
        questionId: String
    ): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM saved_revisions
            WHERE questionId = :questionId
              AND content = :content
        )
        """
    )
    suspend fun revisionContentExistsForQuestion(
        questionId: String,
        content: String
    ): Boolean

    @Query(
        """
        DELETE FROM saved_revisions
        WHERE id = :revisionId
        """
    )
    suspend fun deleteRevisionById(
        revisionId: String
    )

    @Query(
        """
        DELETE FROM saved_revisions
        WHERE attemptId = :attemptId
        """
    )
    suspend fun deleteRevisionsForAttempt(
        attemptId: String
    )

    @Query(
        """
        DELETE FROM saved_revisions
        WHERE questionId = :questionId
        """
    )
    suspend fun deleteRevisionsForQuestion(
        questionId: String
    )

    @Transaction
    suspend fun insertNextRevision(
        id: String,
        attemptId: String,
        questionId: String,
        content: String,
        source: String,
        createdAt: Long
    ): SavedRevisionEntity {
        val existingRevision =
            getRevisionWithContent(
                questionId =
                    questionId,
                content =
                    content
            )

        check(existingRevision == null) {
            "This revision is already saved."
        }

        val nextVersion =
            getLatestVersionNumberForQuestion(
                questionId =
                    questionId
            ) + 1

        val revision =
            SavedRevisionEntity(
                id =
                    id,
                attemptId =
                    attemptId,
                questionId =
                    questionId,
                content =
                    content,
                versionNumber =
                    nextVersion,
                source =
                    source,
                createdAt =
                    createdAt
            )

        insertRevision(
            revision =
                revision
        )

        return revision
    }

    @Query(
        """
        SELECT *
        FROM saved_revisions
        WHERE questionId = :questionId
          AND content = :content
        LIMIT 1
        """
    )
    suspend fun getRevisionWithContent(
        questionId: String,
        content: String
    ): SavedRevisionEntity?
}