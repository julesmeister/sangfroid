package com.julesmeister.sangfroid.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.sangfroid.data.local.entity.CandidateProfileEntity
import com.julesmeister.sangfroid.data.local.entity.ProfileSkillEntity

data class StoredCandidateProfile(
    @Embedded
    val profile: CandidateProfileEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val skills: List<ProfileSkillEntity>
)