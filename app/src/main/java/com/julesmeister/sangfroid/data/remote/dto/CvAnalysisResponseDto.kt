// app/src/main/java/com/julesmeister/sangfroid/data/remote/dto/CvAnalysisResponseDto.kt

package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

data class CvAnalysisResponseDto(
    val requestId: String,
    val profileSummary: String,
    val suggestedRoles: List<String>,
    val suggestedExperienceLevel: String?,
    val skills: List<CvSkillDto>,
    val skillMappings: List<CvSkillMappingDto>,
    val certifications: List<CvCertificationDto>,
    val projects: List<CvProjectDto>,
    val achievements: List<CvAchievementDto>,
    val workHighlights: List<CvWorkHighlightDto>,
    val experienceSignals: List<String>,
    val modelName: String,
    val modelVersion: String?,
    val analyzedAt: Long
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvAnalysisResponseDto {
            return CvAnalysisResponseDto(
                requestId =
                    json.requiredString(
                        "requestId"
                    ),
                profileSummary =
                    json.requiredString(
                        "profileSummary"
                    ),
                suggestedRoles =
                    json.optJSONArray(
                        "suggestedRoles"
                    ).toStringList(),
                suggestedExperienceLevel =
                    json.optionalString(
                        "suggestedExperienceLevel"
                    ),
                skills =
                    json.optJSONArray(
                        "skills"
                    ).toObjectList(
                        CvSkillDto::fromJson
                    ),
                skillMappings =
                    json.optJSONArray(
                        "skillMappings"
                    ).toObjectList(
                        CvSkillMappingDto::fromJson
                    ),
                certifications =
                    json.optJSONArray(
                        "certifications"
                    ).toObjectList(
                        CvCertificationDto::fromJson
                    ),
                projects =
                    json.optJSONArray(
                        "projects"
                    ).toObjectList(
                        CvProjectDto::fromJson
                    ),
                achievements =
                    json.optJSONArray(
                        "achievements"
                    ).toObjectList(
                        CvAchievementDto::fromJson
                    ),
                workHighlights =
                    json.optJSONArray(
                        "workHighlights"
                    ).toObjectList(
                        CvWorkHighlightDto::fromJson
                    ),
                experienceSignals =
                    json.optJSONArray(
                        "experienceSignals"
                    ).toStringList(),
                modelName =
                    json.requiredString(
                        "modelName"
                    ),
                modelVersion =
                    json.optionalString(
                        "modelVersion"
                    ),
                analyzedAt =
                    json.requiredLong(
                        "analyzedAt"
                    )
            )
        }

        private fun JSONObject.requiredString(
            name: String
        ): String {
            val value =
                optString(
                    name,
                    ""
                ).trim()

            require(
                value.isNotBlank()
            ) {
                "$name is missing from the CV-analysis response."
            }

            return value
        }

        private fun JSONObject.optionalString(
            name: String
        ): String? {
            if (
                !has(name) ||
                isNull(name)
            ) {
                return null
            }

            return optString(
                name,
                ""
            )
                .trim()
                .takeIf {
                    it.isNotBlank() &&
                            !it.equals(
                                "null",
                                ignoreCase = true
                            )
                }
        }

        private fun JSONObject.requiredLong(
            name: String
        ): Long {
            require(
                has(name) &&
                        !isNull(name)
            ) {
                "$name is missing from the CV-analysis response."
            }

            return getLong(
                name
            )
        }

        private fun JSONArray?.toStringList():
                List<String> {
            if (this == null) {
                return emptyList()
            }

            return buildList {
                repeat(
                    length()
                ) { index ->
                    val value =
                        optString(
                            index,
                            ""
                        ).trim()

                    if (value.isNotBlank()) {
                        add(
                            value
                        )
                    }
                }
            }.distinct()
        }

        private fun <T> JSONArray?.toObjectList(
            mapper: (JSONObject) -> T
        ): List<T> {
            if (this == null) {
                return emptyList()
            }

            return buildList {
                repeat(
                    length()
                ) { index ->
                    val value =
                        optJSONObject(
                            index
                        )
                            ?: return@repeat

                    add(
                        mapper(
                            value
                        )
                    )
                }
            }
        }
    }
}

data class CvSkillDto(
    val name: String,
    val normalizedName: String,
    val type: String,
    val confidence: Double,
    val evidence: String
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvSkillDto {
            return CvSkillDto(
                name =
                    json.getString(
                        "name"
                    ).trim(),
                normalizedName =
                    json.getString(
                        "normalizedName"
                    ).trim(),
                type =
                    json.getString(
                        "type"
                    ).trim(),
                confidence =
                    json.getDouble(
                        "confidence"
                    ).coerceIn(
                        0.0,
                        1.0
                    ),
                evidence =
                    json.getString(
                        "evidence"
                    ).trim()
            )
        }
    }
}

data class CvSkillMappingDto(
    val extractedSkillName: String,
    val skillCode: String,
    val confidence: Double,
    val reason: String
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvSkillMappingDto {
            return CvSkillMappingDto(
                extractedSkillName =
                    json.getString(
                        "extractedSkillName"
                    ).trim(),
                skillCode =
                    json.getString(
                        "skillCode"
                    ).trim(),
                confidence =
                    json.getDouble(
                        "confidence"
                    ).coerceIn(
                        0.0,
                        1.0
                    ),
                reason =
                    json.optString(
                        "reason",
                        ""
                    ).trim()
            )
        }
    }
}

data class CvCertificationDto(
    val id: String,
    val name: String,
    val issuer: String?,
    val level: String?,
    val status: String,
    val issuedYear: Int?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvCertificationDto {
            return CvCertificationDto(
                id =
                    json.getString(
                        "id"
                    ).trim(),
                name =
                    json.getString(
                        "name"
                    ).trim(),
                issuer =
                    json.optionalText(
                        "issuer"
                    ),
                level =
                    json.optionalText(
                        "level"
                    ),
                status =
                    json.optString(
                        "status",
                        "UNKNOWN"
                    )
                        .trim()
                        .uppercase(),
                issuedYear =
                    json.optionalInt(
                        "issuedYear"
                    ),
                evidence =
                    json.getString(
                        "evidence"
                    ).trim(),
                relatedSkills =
                    json.optJSONArray(
                        "relatedSkills"
                    ).toStrings()
            )
        }
    }
}

data class CvProjectDto(
    val id: String,
    val name: String,
    val context: String?,
    val candidateRole: String?,
    val objective: String?,
    val actions: List<String>,
    val methods: List<String>,
    val tools: List<String>,
    val outcomes: List<String>,
    val metrics: List<String>,
    val relatedSkills: List<String>,
    val evidence: String
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvProjectDto {
            return CvProjectDto(
                id =
                    json.getString(
                        "id"
                    ).trim(),
                name =
                    json.getString(
                        "name"
                    ).trim(),
                context =
                    json.optionalText(
                        "context"
                    ),
                candidateRole =
                    json.optionalText(
                        "candidateRole"
                    ),
                objective =
                    json.optionalText(
                        "objective"
                    ),
                actions =
                    json.optJSONArray(
                        "actions"
                    ).toStrings(),
                methods =
                    json.optJSONArray(
                        "methods"
                    ).toStrings(),
                tools =
                    json.optJSONArray(
                        "tools"
                    ).toStrings(),
                outcomes =
                    json.optJSONArray(
                        "outcomes"
                    ).toStrings(),
                metrics =
                    json.optJSONArray(
                        "metrics"
                    ).toStrings(),
                relatedSkills =
                    json.optJSONArray(
                        "relatedSkills"
                    ).toStrings(),
                evidence =
                    json.getString(
                        "evidence"
                    ).trim()
            )
        }
    }
}

data class CvAchievementDto(
    val id: String,
    val title: String,
    val context: String?,
    val candidateAction: String?,
    val result: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvAchievementDto {
            return CvAchievementDto(
                id =
                    json.getString(
                        "id"
                    ).trim(),
                title =
                    json.getString(
                        "title"
                    ).trim(),
                context =
                    json.optionalText(
                        "context"
                    ),
                candidateAction =
                    json.optionalText(
                        "candidateAction"
                    ),
                result =
                    json.optionalText(
                        "result"
                    ),
                metric =
                    json.optionalText(
                        "metric"
                    ),
                evidence =
                    json.getString(
                        "evidence"
                    ).trim(),
                relatedSkills =
                    json.optJSONArray(
                        "relatedSkills"
                    ).toStrings()
            )
        }
    }
}

data class CvWorkHighlightDto(
    val id: String,
    val title: String,
    val organizationContext: String?,
    val candidateRole: String?,
    val responsibility: String?,
    val action: String?,
    val outcome: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    companion object {

        fun fromJson(
            json: JSONObject
        ): CvWorkHighlightDto {
            return CvWorkHighlightDto(
                id =
                    json.getString(
                        "id"
                    ).trim(),
                title =
                    json.getString(
                        "title"
                    ).trim(),
                organizationContext =
                    json.optionalText(
                        "organizationContext"
                    ),
                candidateRole =
                    json.optionalText(
                        "candidateRole"
                    ),
                responsibility =
                    json.optionalText(
                        "responsibility"
                    ),
                action =
                    json.optionalText(
                        "action"
                    ),
                outcome =
                    json.optionalText(
                        "outcome"
                    ),
                metric =
                    json.optionalText(
                        "metric"
                    ),
                evidence =
                    json.getString(
                        "evidence"
                    ).trim(),
                relatedSkills =
                    json.optJSONArray(
                        "relatedSkills"
                    ).toStrings()
            )
        }
    }
}

private fun JSONObject.optionalText(
    name: String
): String? {
    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }

    return optString(
        name,
        ""
    )
        .trim()
        .takeIf(
            String::isNotBlank
        )
}

private fun JSONObject.optionalInt(
    name: String
): Int? {
    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }

    return runCatching {
        getInt(
            name
        )
    }.getOrNull()
}

private fun JSONArray?.toStrings():
        List<String> {
    if (this == null) {
        return emptyList()
    }

    return buildList {
        repeat(
            length()
        ) { index ->
            val value =
                optString(
                    index,
                    ""
                ).trim()

            if (value.isNotBlank()) {
                add(
                    value
                )
            }
        }
    }.distinct()
}