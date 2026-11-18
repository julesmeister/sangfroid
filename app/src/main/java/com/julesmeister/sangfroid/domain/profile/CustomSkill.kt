package com.julesmeister.sangfroid.domain.profile

import java.util.Locale

/** A skill that has no SkillCode, e.g. "Docker" extracted from a CV. */
data class CustomSkill(
    val name: String,
    val source: ProfileSkillSource
) {
    val normalizedName: String
        get() = normalize(name)

    companion object {
        const val MAXIMUM_NAME_LENGTH = 60

        fun normalize(value: String): String =
            value
                .trim()
                .lowercase(Locale.ROOT)
                .replace(Regex("\\s+"), " ")

        fun clean(value: String): String =
            value
                .trim()
                .replace(Regex("\\s+"), " ")
                .take(MAXIMUM_NAME_LENGTH)
    }
}