package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import java.util.Locale

data class ResolvedCvSkills(
    val skillCodes: List<SkillCode>,
    val customSkillNames: List<String>,
    private val strengthMatches: Map<String, SkillCode?>
) {

    private val customKeys: Set<String> =
        customSkillNames
            .asSequence()
            .map(
                CustomSkill::normalize
            )
            .filter(
                String::isNotBlank
            )
            .toSet()

    fun matchFor(
        strengthName: String
    ): SkillCode? {
        val key =
            CustomSkill.normalize(
                strengthName
            )

        return strengthMatches[
            key
        ]
    }

    fun isCustom(
        strengthName: String
    ): Boolean {
        val key =
            CustomSkill.normalize(
                strengthName
            )

        return matchFor(
            strengthName
        ) == null &&
                key in customKeys
    }
}

/**
 * Single source of truth for what a CV contributes to the editable profile.
 *
 * Catalogue skills come from:
 * 1. backend-provided skill mappings;
 * 2. direct SkillCode-name matching;
 * 3. controlled aliases.
 *
 * CV strengths that do not map to the catalogue are retained as custom
 * profile skills instead of being discarded.
 */
fun CvProfile.resolveSkillCodes():
        ResolvedCvSkills {
    val resolvedCodes =
        LinkedHashSet<SkillCode>()

    val strengthMatches =
        LinkedHashMap<
                String,
                SkillCode?
                >()

    val mappingsByStrength =
        LinkedHashMap<
                String,
                SkillCode
                >()

    skillMappings
        .asSequence()
        .filter { mapping ->
            mapping.confidence >=
                    MINIMUM_MAPPING_CONFIDENCE
        }
        .sortedByDescending { mapping ->
            mapping.confidence
        }
        .forEach { mapping ->
            val code = mapping.skillCode

            resolvedCodes +=
                code

            val extractedName =
                CustomSkill.clean(
                    mapping.extractedSkillName
                )

            val extractedKey =
                CustomSkill.normalize(
                    extractedName
                )

            if (
                extractedKey.isNotBlank() &&
                extractedKey !in mappingsByStrength
            ) {
                mappingsByStrength[
                    extractedKey
                ] =
                    code
            }
        }

    val customSkills =
        LinkedHashMap<
                String,
                String
                >()

    skills
        .asSequence()
        .sortedByDescending { skill ->
            skill.confidence
        }
        .forEach { skill ->
            val cleanName =
                CustomSkill.clean(
                    skill.name
                )

            val fallbackName =
                CustomSkill.clean(
                    skill.normalizedName
                )

            val displayName =
                cleanName
                    .ifBlank {
                        fallbackName
                    }

            val key =
                CustomSkill.normalize(
                    displayName
                )

            if (key.isBlank()) {
                return@forEach
            }

            val code =
                mappingsByStrength[
                    key
                ]
                    ?: matchSkillCode(
                        skill.name
                    )
                    ?: matchSkillCode(
                        skill.normalizedName
                    )

            strengthMatches[
                key
            ] =
                code

            if (code != null) {
                resolvedCodes +=
                    code

                customSkills.remove(
                    key
                )
            } else if (
                displayName.isNotBlank() &&
                key !in customSkills
            ) {
                customSkills[
                    key
                ] =
                    displayName
            }
        }

    /*
     * A backend mapping may use a slightly different extracted name than
     * the corresponding CvSkill name. Preserve those mappings in the
     * lookup table so UI calls such as matchFor(mappingName) still work.
     */
    mappingsByStrength.forEach {
            (
                key,
                code
            ) ->

        if (
            key !in
            strengthMatches
        ) {
            strengthMatches[
                key
            ] =
                code
        }
    }

    return ResolvedCvSkills(
        skillCodes =
            resolvedCodes
                .toList(),
        customSkillNames =
            customSkills
                .values
                .toList(),
        strengthMatches =
            strengthMatches
                .toMap()
    )
}

private fun skillCodeFromName(
    value: String
): SkillCode? {
    val normalized =
        normalizeEnumName(
            value
        )

    if (normalized.isBlank()) {
        return null
    }

    val directMatch =
        SkillCode.entries
            .firstOrNull { skillCode ->
                skillCode.name ==
                        normalized
            }

    if (directMatch != null) {
        return directMatch
    }

    val compatibilityName =
        SKILL_CODE_COMPATIBILITY_ALIASES[
            normalized
        ]

    if (compatibilityName != null) {
        return SkillCode.entries
            .firstOrNull { skillCode ->
                skillCode.name ==
                        compatibilityName
            }
    }

    if (
        normalized.endsWith(
            "S"
        )
    ) {
        val singular =
            normalized.dropLast(
                1
            )

        return SkillCode.entries
            .firstOrNull { skillCode ->
                skillCode.name ==
                        singular
            }
    }

    return null
}

private fun matchSkillCode(
    text: String
): SkillCode? {
    skillCodeFromName(
        text
    )?.let { directMatch ->
        return directMatch
    }

    val aliasKey =
        normalizeAlias(
            text
        )

    if (aliasKey.isBlank()) {
        return null
    }

    SKILL_ALIASES[
        aliasKey
    ]?.let { mappedName ->
        return skillCodeFromName(
            mappedName
        )
    }

    /*
     * Attempt exact token matching against catalogue names after alias
     * normalization. This supports values such as "Spring Boot",
     * "Problem Solving", and "System Design" without a dedicated alias.
     */
    return SkillCode.entries
        .firstOrNull { skillCode ->
            normalizeAlias(
                skillCode.name
            ) ==
                    aliasKey
        }
}

private fun normalizeEnumName(
    value: String
): String {
    return value
        .trim()
        .uppercase(
            Locale.ROOT
        )
        .replace(
            Regex(
                "[^A-Z0-9]+"
            ),
            "_"
        )
        .trim(
            '_'
        )
}

private fun normalizeAlias(
    value: String
): String {
    return value
        .trim()
        .lowercase(
            Locale.ROOT
        )
        .replace(
            Regex(
                "[^a-z0-9]+"
            ),
            " "
        )
        .trim()
        .replace(
            Regex(
                "\\s+"
            ),
            " "
        )
}

private val SKILL_CODE_COMPATIBILITY_ALIASES:
        Map<String, String> =
    mapOf(
        "GENERAL_COMMUNICATION" to
                "COMMUNICATION",
        "REST" to
                "REST_API",
        "RESTFUL_API" to
                "REST_API",
        "RESTFUL_APIS" to
                "REST_API",
        "NO_SQL" to
                "NOSQL",
        "DYNAMO_DB" to
                "DYNAMODB",
        "GITHUB" to
                "GIT",
        "GITLAB" to
                "GIT",
        "SPRING" to
                "SPRING_BOOT",
        "TEAM_WORK" to
                "TEAMWORK",
        "CONFLICT_MANAGEMENT" to
                "CONFLICT_RESOLUTION",
        "ACCOUNTABILITY" to
                "OWNERSHIP",
        "TROUBLESHOOTING" to
                "PROBLEM_SOLVING",
        "UNIT_TESTING" to
                "TESTING",
        "AUTOMATED_TESTING" to
                "TESTING"
    )

private val SKILL_ALIASES:
        Map<String, String> =
    mapOf(
        "amazon web services" to
                "AWS",
        "aws cloud" to
                "AWS",
        "ci cd" to
                "CI_CD",
        "continuous integration" to
                "CI_CD",
        "continuous delivery" to
                "CI_CD",
        "continuous deployment" to
                "CI_CD",
        "rest" to
                "REST_API",
        "rest api" to
                "REST_API",
        "rest apis" to
                "REST_API",
        "restful api" to
                "REST_API",
        "restful apis" to
                "REST_API",
        "no sql" to
                "NOSQL",
        "nosql database" to
                "NOSQL",
        "dynamo db" to
                "DYNAMODB",
        "amazon dynamodb" to
                "DYNAMODB",
        "team work" to
                "TEAMWORK",
        "written communication" to
                "COMMUNICATION",
        "verbal communication" to
                "COMMUNICATION",
        "oral communication" to
                "COMMUNICATION",
        "stakeholder communication" to
                "COMMUNICATION",
        "interpersonal communication" to
                "COMMUNICATION",
        "team leadership" to
                "LEADERSHIP",
        "people leadership" to
                "LEADERSHIP",
        "people management" to
                "LEADERSHIP",
        "conflict management" to
                "CONFLICT_RESOLUTION",
        "conflict handling" to
                "CONFLICT_RESOLUTION",
        "accountability" to
                "OWNERSHIP",
        "taking ownership" to
                "OWNERSHIP",
        "troubleshooting" to
                "PROBLEM_SOLVING",
        "analytical problem solving" to
                "PROBLEM_SOLVING",
        "unit testing" to
                "TESTING",
        "automated testing" to
                "TESTING",
        "test automation" to
                "TESTING",
        "spring" to
                "SPRING_BOOT",
        "spring framework" to
                "SPRING_BOOT",
        "jetpack compose" to
                "ANDROID",
        "android development" to
                "ANDROID",
        "android app development" to
                "ANDROID",
        "github" to
                "GIT",
        "gitlab" to
                "GIT",
        "version control" to
                "GIT",
        "source control" to
                "GIT"
    )

private const val MINIMUM_MAPPING_CONFIDENCE =
    0.50