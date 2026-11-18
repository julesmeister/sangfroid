package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.domain.model.SkillCode

/** Which skills exist, how they're grouped, labelled and keyed. */
object SkillCatalog {

    private const val CUSTOM_PREFIX = "custom:"

    val peopleSkills: List<SkillCode> =
        listOf(
            SkillCode.GENERAL,
            SkillCode.COMMUNICATION,
            SkillCode.SELF_INTRODUCTION,
            SkillCode.MOTIVATION,
            SkillCode.LEADERSHIP,
            SkillCode.TEAMWORK,
            SkillCode.CONFLICT_RESOLUTION,
            SkillCode.PROBLEM_SOLVING,
            SkillCode.TIME_MANAGEMENT,
            SkillCode.ADAPTABILITY,
            SkillCode.OWNERSHIP,
            SkillCode.FEEDBACK
        )

    val technicalSkills: List<SkillCode> =
        SkillCode.entries.filterNot { it in peopleSkills }

    val experienceLevels: List<String> =
        listOf("Intern", "Junior", "Associate", "Mid-level", "Senior", "Lead")

    fun key(code: SkillCode): String = code.name

    fun customKey(normalizedName: String): String = CUSTOM_PREFIX + normalizedName

    fun fromKey(key: String): SkillCode? =
        SkillCode.entries.firstOrNull { it.name == key }

    /** Returns the normalized custom-skill name for a custom key, or null. */
    fun customNameFromKey(key: String): String? =
        if (key.startsWith(CUSTOM_PREFIX)) key.removePrefix(CUSTOM_PREFIX) else null

    fun label(code: SkillCode): String {
        return when (code) {
            SkillCode.CI_CD -> "CI/CD"
            SkillCode.REST_API -> "REST API"
            SkillCode.HTTP -> "HTTP"
            SkillCode.SQL -> "SQL"
            SkillCode.NOSQL -> "NoSQL"
            SkillCode.DYNAMODB -> "DynamoDB"
            SkillCode.AWS -> "AWS"
            else -> code.name
                .lowercase()
                .split("_")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
        }
    }
}