package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.SyncOperation

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["nextAttemptAt"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: SyncOperation,
    val payload: String,
    val attemptCount: Int = 0,
    val nextAttemptAt: Long?,
    val lastError: String?,
    val createdAt: Long = System.currentTimeMillis()
)