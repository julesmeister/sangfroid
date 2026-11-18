    package com.julesmeister.sangfroid.data.local.entity

    import androidx.room.Entity
    import androidx.room.ForeignKey
    import androidx.room.Index
    import com.julesmeister.sangfroid.domain.model.SkillCode
    import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource

    @Entity(
        tableName = "profile_skills",
        primaryKeys = [
            "profileId",
            "skillCode",
            "source"
        ],
        foreignKeys = [
            ForeignKey(
                entity =
                    CandidateProfileEntity::class,
                parentColumns = ["id"],
                childColumns = ["profileId"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(value = ["profileId"]),
            Index(value = ["skillCode"]),
            Index(value = ["source"])
        ]
    )
    data class ProfileSkillEntity(
        val profileId: String,
        val skillCode: SkillCode,
        val source: ProfileSkillSource
    )