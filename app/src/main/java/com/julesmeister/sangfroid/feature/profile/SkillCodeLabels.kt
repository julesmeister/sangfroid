package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.domain.model.SkillCode

fun SkillCode.skillLabel(): String {
    return when (this) {
        SkillCode.CI_CD -> "CI/CD"
        SkillCode.REST_API -> "REST API"
        SkillCode.HTTP -> "HTTP"
        SkillCode.SQL -> "SQL"
        SkillCode.NOSQL -> "NoSQL"
        SkillCode.DYNAMODB -> "DynamoDB"
        SkillCode.AWS -> "AWS"
        else ->
            name
                .lowercase()
                .split("_")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
    }
}