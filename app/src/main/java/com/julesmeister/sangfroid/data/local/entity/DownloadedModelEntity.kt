package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.DownloadedModelType

@Entity(
    tableName = "downloaded_models",
    indices = [
        Index(value = ["modelType"]),
        Index(value = ["isSelected"])
    ]
)
data class DownloadedModelEntity(
    @PrimaryKey
    val id: String,
    val modelType: DownloadedModelType,
    val displayName: String,
    val modelName: String,
    val modelVersion: String,
    val localDirectoryPath: String,
    val sizeBytes: Long,
    val languageTag: String?,
    val isDownloaded: Boolean,
    val isSelected: Boolean,
    val downloadedAt: Long?
)