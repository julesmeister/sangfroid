package com.julesmeister.sangfroid.data.remote.dto

import com.julesmeister.sangfroid.domain.recommendation.CvEvidenceType
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class QuestionGenerationRequestDto(
    val requestId: String,
    val requestedCount: Int,
    val targetRole: String?,
    val experienceLevel: String?,
    val cvSkills: List<String>,
    val cvEvidence: List<CvEvidenceDto>,
    val weakSkills: List<WeakSkillDto>,
    val generationPlans: List<QuestionGenerationPlanDto>,
    val existingQuestions: List<String>,
    val recentQuestionIds: List<String>,
    val savedSkillCodes: List<String>,
    val skippedCategories: List<String>,
    val preferredCategories: List<String>,
    val explorationRatio: Float,
    val languageTag: String
) {

    init {
        require(requestId.isNotBlank()) {
            "requestId cannot be blank."
        }

        require(
            requestId.length <=
                    MAXIMUM_REQUEST_ID_LENGTH
        ) {
            "requestId cannot exceed 100 characters."
        }

        require(requestedCount in 1..10) {
            "requestedCount must be between one and ten."
        }

        require(
            cvSkills.size <=
                    MAXIMUM_CV_SKILL_COUNT
        ) {
            "cvSkills cannot contain more than 100 values."
        }

        require(
            cvEvidence.size <=
                    MAXIMUM_CV_EVIDENCE_COUNT
        ) {
            "cvEvidence cannot contain more than 30 values."
        }

        require(
            weakSkills.size <=
                    MAXIMUM_WEAK_SKILL_COUNT
        ) {
            "weakSkills cannot contain more than 100 values."
        }

        require(
            generationPlans.size <=
                    MAXIMUM_GENERATION_PLAN_COUNT
        ) {
            "generationPlans cannot contain more than 30 values."
        }

        require(
            existingQuestions.size <=
                    MAXIMUM_EXISTING_QUESTION_COUNT
        ) {
            "existingQuestions cannot contain more than 250 values."
        }

        require(
            recentQuestionIds.size <=
                    MAXIMUM_RECENT_QUESTION_COUNT
        ) {
            "recentQuestionIds cannot contain more than 100 values."
        }

        require(
            savedSkillCodes.size <=
                    MAXIMUM_SAVED_SKILL_COUNT
        ) {
            "savedSkillCodes cannot contain more than 100 values."
        }

        require(
            skippedCategories.size <=
                    MAXIMUM_CATEGORY_COUNT
        ) {
            "skippedCategories cannot contain more than 20 values."
        }

        require(
            preferredCategories.size <=
                    MAXIMUM_CATEGORY_COUNT
        ) {
            "preferredCategories cannot contain more than 20 values."
        }

        require(
            explorationRatio.isFinite() &&
                    explorationRatio in 0f..1f
        ) {
            "explorationRatio must be between zero and one."
        }

        require(languageTag.isNotBlank()) {
            "languageTag cannot be blank."
        }

        require(
            languageTag.length <=
                    MAXIMUM_LANGUAGE_TAG_LENGTH
        ) {
            "languageTag cannot exceed 20 characters."
        }
    }

    fun toJson(): JSONObject {
        val normalizedEvidence =
            cvEvidence
                .map(CvEvidenceDto::normalized)
                .filter { evidence ->
                    evidence.id.isNotBlank() &&
                            evidence.title.isNotBlank()
                }
                .distinctBy { evidence ->
                    evidence.type.name to
                            evidence.id
                }
                .take(
                    MAXIMUM_CV_EVIDENCE_COUNT
                )

        val allowedEvidenceKeys =
            normalizedEvidence
                .mapTo(mutableSetOf()) { evidence ->
                    EvidenceKey(
                        type =
                            evidence.type.name,
                        id =
                            evidence.id
                    )
                }

        val normalizedGenerationPlans =
            generationPlans
                .map(
                    QuestionGenerationPlanDto::normalized
                )
                .filter { plan ->
                    plan.evidenceType
                        .isNotBlank() &&
                            plan.evidenceId
                                .isNotBlank()
                }
                .filter { plan ->
                    EvidenceKey(
                        type =
                            plan.evidenceType,
                        id =
                            plan.evidenceId
                    ) in allowedEvidenceKeys
                }
                .distinctBy { plan ->
                    plan.evidenceType to
                            plan.evidenceId
                }
                .take(
                    MAXIMUM_GENERATION_PLAN_COUNT
                )

        return JSONObject().apply {
            put(
                JSON_REQUEST_ID,
                requestId
                    .trim()
                    .take(
                        MAXIMUM_REQUEST_ID_LENGTH
                    )
            )

            put(
                JSON_REQUESTED_COUNT,
                requestedCount
            )

            putNullable(
                JSON_TARGET_ROLE,
                targetRole,
                MAXIMUM_TARGET_ROLE_LENGTH
            )

            putNullable(
                JSON_EXPERIENCE_LEVEL,
                experienceLevel,
                MAXIMUM_EXPERIENCE_LEVEL_LENGTH
            )

            put(
                JSON_CV_SKILLS,
                JSONArray(
                    cvSkills.normalizeEnumList(
                        maximumItems =
                            MAXIMUM_CV_SKILL_COUNT
                    )
                )
            )

            put(
                JSON_CV_EVIDENCE,
                JSONArray().apply {
                    normalizedEvidence.forEach { evidence ->
                        put(
                            evidence.toJson()
                        )
                    }
                }
            )

            put(
                JSON_WEAK_SKILLS,
                JSONArray().apply {
                    weakSkills
                        .take(
                            MAXIMUM_WEAK_SKILL_COUNT
                        )
                        .forEach { weakSkill ->
                            put(
                                weakSkill.toJson()
                            )
                        }
                }
            )

            put(
                JSON_GENERATION_PLANS,
                JSONArray().apply {
                    normalizedGenerationPlans
                        .forEach { plan ->
                            put(
                                plan.toJson()
                            )
                        }
                }
            )

            put(
                JSON_EXISTING_QUESTIONS,
                JSONArray(
                    existingQuestions
                        .normalizeTextList(
                            maximumItems =
                                MAXIMUM_EXISTING_QUESTION_COUNT,
                            maximumLength =
                                MAXIMUM_EXISTING_QUESTION_LENGTH
                        )
                )
            )

            put(
                JSON_RECENT_QUESTION_IDS,
                JSONArray(
                    recentQuestionIds
                        .normalizeTextList(
                            maximumItems =
                                MAXIMUM_RECENT_QUESTION_COUNT,
                            maximumLength =
                                MAXIMUM_IDENTIFIER_LENGTH
                        )
                )
            )

            put(
                JSON_SAVED_SKILL_CODES,
                JSONArray(
                    savedSkillCodes
                        .normalizeEnumList(
                            maximumItems =
                                MAXIMUM_SAVED_SKILL_COUNT
                        )
                )
            )

            put(
                JSON_SKIPPED_CATEGORIES,
                JSONArray(
                    skippedCategories
                        .normalizeEnumList(
                            maximumItems =
                                MAXIMUM_CATEGORY_COUNT
                        )
                )
            )

            put(
                JSON_PREFERRED_CATEGORIES,
                JSONArray(
                    preferredCategories
                        .normalizeEnumList(
                            maximumItems =
                                MAXIMUM_CATEGORY_COUNT
                        )
                )
            )

            put(
                JSON_EXPLORATION_RATIO,
                explorationRatio.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
            )

            put(
                JSON_LANGUAGE_TAG,
                languageTag
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .trim()
                    .take(
                        MAXIMUM_LANGUAGE_TAG_LENGTH
                    )
            )
        }
    }

    private fun JSONObject.putNullable(
        name: String,
        value: String?,
        maximumLength: Int
    ) {
        val normalized =
            value.normalizeOptional(
                maximumLength =
                    maximumLength
            )

        put(
            name,
            normalized
                ?: JSONObject.NULL
        )
    }

    private fun String?.normalizeOptional(
        maximumLength: Int
    ): String? {
        return this
            ?.replace(
                '\u0000',
                ' '
            )
            ?.replace(
                WHITESPACE_REGEX,
                " "
            )
            ?.trim()
            ?.take(
                maximumLength
            )
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun List<String>.normalizeTextList(
        maximumItems: Int,
        maximumLength: Int
    ): List<String> {
        return asSequence()
            .map { value ->
                value
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .replace(
                        WHITESPACE_REGEX,
                        " "
                    )
                    .trim()
                    .take(
                        maximumLength
                    )
            }
            .filter(
                String::isNotBlank
            )
            .distinctBy { value ->
                value.lowercase(
                    Locale.ROOT
                )
            }
            .take(
                maximumItems
            )
            .toList()
    }

    private fun List<String>.normalizeEnumList(
        maximumItems: Int
    ): List<String> {
        return asSequence()
            .map { value ->
                value.normalizeEnumValue()
            }
            .filter(
                String::isNotBlank
            )
            .distinct()
            .take(
                maximumItems
            )
            .toList()
    }

    private fun String.normalizeEnumValue():
            String {
        return replace(
            '\u0000',
            ' '
        )
            .trim()
            .uppercase(
                Locale.ROOT
            )
            .replace(
                ENUM_SEPARATOR_REGEX,
                "_"
            )
            .trim(
                '_'
            )
    }

    private data class EvidenceKey(
        val type: String,
        val id: String
    )

    companion object {
        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_REQUESTED_COUNT =
            "requestedCount"

        private const val JSON_TARGET_ROLE =
            "targetRole"

        private const val JSON_EXPERIENCE_LEVEL =
            "experienceLevel"

        private const val JSON_CV_SKILLS =
            "cvSkills"

        private const val JSON_CV_EVIDENCE =
            "cvEvidence"

        private const val JSON_WEAK_SKILLS =
            "weakSkills"

        private const val JSON_GENERATION_PLANS =
            "generationPlans"

        private const val JSON_EXISTING_QUESTIONS =
            "existingQuestions"

        private const val JSON_RECENT_QUESTION_IDS =
            "recentQuestionIds"

        private const val JSON_SAVED_SKILL_CODES =
            "savedSkillCodes"

        private const val JSON_SKIPPED_CATEGORIES =
            "skippedCategories"

        private const val JSON_PREFERRED_CATEGORIES =
            "preferredCategories"

        private const val JSON_EXPLORATION_RATIO =
            "explorationRatio"

        private const val JSON_LANGUAGE_TAG =
            "languageTag"

        private const val MAXIMUM_REQUEST_ID_LENGTH =
            100

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            200

        private const val MAXIMUM_EXPERIENCE_LEVEL_LENGTH =
            100

        private const val MAXIMUM_CV_SKILL_COUNT =
            100

        private const val MAXIMUM_CV_EVIDENCE_COUNT =
            30

        private const val MAXIMUM_WEAK_SKILL_COUNT =
            100

        private const val MAXIMUM_GENERATION_PLAN_COUNT =
            30

        private const val MAXIMUM_EXISTING_QUESTION_COUNT =
            250

        private const val MAXIMUM_EXISTING_QUESTION_LENGTH =
            500

        private const val MAXIMUM_RECENT_QUESTION_COUNT =
            100

        private const val MAXIMUM_SAVED_SKILL_COUNT =
            100

        private const val MAXIMUM_CATEGORY_COUNT =
            20

        private const val MAXIMUM_IDENTIFIER_LENGTH =
            100

        private const val MAXIMUM_LANGUAGE_TAG_LENGTH =
            20

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}

data class CvEvidenceDto(
    val id: String,
    val type: CvEvidenceType,
    val title: String,
    val context: String?,
    val candidateRole: String?,
    val actions: List<String>,
    val methods: List<String>,
    val tools: List<String>,
    val outcomes: List<String>,
    val metrics: List<String>,
    val relatedSkills: List<String>,
    val evidence: String?
) {

    fun normalized(): CvEvidenceDto {
        return copy(
            id =
                id.normalizeRequiredText(
                    maximumLength =
                        MAXIMUM_ID_LENGTH
                ),
            title =
                title.normalizeRequiredText(
                    maximumLength =
                        MAXIMUM_TITLE_LENGTH
                ),
            context =
                context.normalizeOptional(
                    maximumLength =
                        MAXIMUM_CONTEXT_LENGTH
                ),
            candidateRole =
                candidateRole.normalizeOptional(
                    maximumLength =
                        MAXIMUM_ROLE_LENGTH
                ),
            actions =
                actions.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_COUNT,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            methods =
                methods.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_COUNT,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            tools =
                tools.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_COUNT,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            outcomes =
                outcomes.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_COUNT,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            metrics =
                metrics.normalizeList(
                    maximumItems =
                        MAXIMUM_METRIC_COUNT,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            relatedSkills =
                relatedSkills.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_COUNT,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            evidence =
                evidence.normalizeOptional(
                    maximumLength =
                        MAXIMUM_EVIDENCE_LENGTH
                )
        )
    }

    fun toJson(): JSONObject {
        val normalized =
            normalized()

        return JSONObject().apply {
            put(
                JSON_ID,
                normalized.id
            )

            put(
                JSON_TYPE,
                normalized.type.name
            )

            put(
                JSON_TITLE,
                normalized.title
            )

            putNullable(
                JSON_CONTEXT,
                normalized.context
            )

            putNullable(
                JSON_CANDIDATE_ROLE,
                normalized.candidateRole
            )

            put(
                JSON_ACTIONS,
                JSONArray(
                    normalized.actions
                )
            )

            put(
                JSON_METHODS,
                JSONArray(
                    normalized.methods
                )
            )

            put(
                JSON_TOOLS,
                JSONArray(
                    normalized.tools
                )
            )

            put(
                JSON_OUTCOMES,
                JSONArray(
                    normalized.outcomes
                )
            )

            put(
                JSON_METRICS,
                JSONArray(
                    normalized.metrics
                )
            )

            put(
                JSON_RELATED_SKILLS,
                JSONArray(
                    normalized.relatedSkills
                )
            )

            putNullable(
                JSON_EVIDENCE,
                normalized.evidence
            )
        }
    }

    private fun JSONObject.putNullable(
        name: String,
        value: String?
    ) {
        put(
            name,
            value
                ?: JSONObject.NULL
        )
    }

    private fun String.normalizeRequiredText(
        maximumLength: Int
    ): String {
        return replace(
            '\u0000',
            ' '
        )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
            .take(
                maximumLength
            )
    }

    private fun String?.normalizeOptional(
        maximumLength: Int
    ): String? {
        return this
            ?.replace(
                '\u0000',
                ' '
            )
            ?.replace(
                WHITESPACE_REGEX,
                " "
            )
            ?.trim()
            ?.take(
                maximumLength
            )
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun List<String>.normalizeList(
        maximumItems: Int,
        maximumLength: Int
    ): List<String> {
        return asSequence()
            .map { value ->
                value
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .replace(
                        WHITESPACE_REGEX,
                        " "
                    )
                    .trim()
                    .take(
                        maximumLength
                    )
            }
            .filter(
                String::isNotBlank
            )
            .distinctBy { value ->
                value.lowercase(
                    Locale.ROOT
                )
            }
            .take(
                maximumItems
            )
            .toList()
    }

    companion object {
        private const val JSON_ID =
            "id"

        private const val JSON_TYPE =
            "type"

        private const val JSON_TITLE =
            "title"

        private const val JSON_CONTEXT =
            "context"

        private const val JSON_CANDIDATE_ROLE =
            "candidateRole"

        private const val JSON_ACTIONS =
            "actions"

        private const val JSON_METHODS =
            "methods"

        private const val JSON_TOOLS =
            "tools"

        private const val JSON_OUTCOMES =
            "outcomes"

        private const val JSON_METRICS =
            "metrics"

        private const val JSON_RELATED_SKILLS =
            "relatedSkills"

        private const val JSON_EVIDENCE =
            "evidence"

        private const val MAXIMUM_ID_LENGTH =
            120

        private const val MAXIMUM_TITLE_LENGTH =
            180

        private const val MAXIMUM_CONTEXT_LENGTH =
            240

        private const val MAXIMUM_ROLE_LENGTH =
            160

        private const val MAXIMUM_DETAIL_LENGTH =
            240

        private const val MAXIMUM_SHORT_DETAIL_LENGTH =
            120

        private const val MAXIMUM_EVIDENCE_LENGTH =
            240

        private const val MAXIMUM_GENERAL_LIST_COUNT =
            8

        private const val MAXIMUM_METRIC_COUNT =
            6

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}

data class WeakSkillDto(
    val skillCode: String,
    val averageScore: Float,
    val evidenceCount: Int,
    val missingPoints: List<String>
) {

    init {
        require(
            averageScore.isFinite() &&
                    averageScore in 0f..5f
        ) {
            "averageScore must be between zero and five."
        }

        require(evidenceCount >= 0) {
            "evidenceCount cannot be negative."
        }

        require(
            missingPoints.size <=
                    MAXIMUM_MISSING_POINT_COUNT
        ) {
            "missingPoints cannot contain more than 20 values."
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(
                JSON_SKILL_CODE,
                skillCode
                    .normalizeEnumValue()
                    .take(
                        MAXIMUM_SKILL_CODE_LENGTH
                    )
            )

            put(
                JSON_AVERAGE_SCORE,
                averageScore.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 5f
                )
            )

            put(
                JSON_EVIDENCE_COUNT,
                evidenceCount.coerceIn(
                    minimumValue = 0,
                    maximumValue = 10_000
                )
            )

            put(
                JSON_MISSING_POINTS,
                JSONArray(
                    missingPoints
                        .asSequence()
                        .map { point ->
                            point
                                .replace(
                                    '\u0000',
                                    ' '
                                )
                                .replace(
                                    WHITESPACE_REGEX,
                                    " "
                                )
                                .trim()
                                .take(
                                    MAXIMUM_MISSING_POINT_LENGTH
                                )
                        }
                        .filter(
                            String::isNotBlank
                        )
                        .distinctBy { point ->
                            point.lowercase(
                                Locale.ROOT
                            )
                        }
                        .take(
                            MAXIMUM_MISSING_POINT_COUNT
                        )
                        .toList()
                )
            )
        }
    }

    private fun String.normalizeEnumValue():
            String {
        return replace(
            '\u0000',
            ' '
        )
            .trim()
            .uppercase(
                Locale.ROOT
            )
            .replace(
                ENUM_SEPARATOR_REGEX,
                "_"
            )
            .trim(
                '_'
            )
    }

    companion object {
        private const val JSON_SKILL_CODE =
            "skillCode"

        private const val JSON_AVERAGE_SCORE =
            "averageScore"

        private const val JSON_EVIDENCE_COUNT =
            "evidenceCount"

        private const val JSON_MISSING_POINTS =
            "missingPoints"

        private const val MAXIMUM_SKILL_CODE_LENGTH =
            100

        private const val MAXIMUM_MISSING_POINT_COUNT =
            20

        private const val MAXIMUM_MISSING_POINT_LENGTH =
            500

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}

data class QuestionGenerationPlanDto(
    val evidenceType: String,
    val evidenceId: String,
    val requestedAngles: List<String>,
    val coveredAngles: List<String>,
    val previousQuestionCount: Int
) {

    init {
        require(evidenceType.isNotBlank()) {
            "Generation-plan evidenceType cannot be blank."
        }

        require(evidenceId.isNotBlank()) {
            "Generation-plan evidenceId cannot be blank."
        }

        require(
            requestedAngles.size <=
                    MAXIMUM_ANGLE_COUNT
        ) {
            "requestedAngles cannot contain more than 15 values."
        }

        require(
            coveredAngles.size <=
                    MAXIMUM_ANGLE_COUNT
        ) {
            "coveredAngles cannot contain more than 15 values."
        }

        require(previousQuestionCount >= 0) {
            "previousQuestionCount cannot be negative."
        }
    }

    fun normalized():
            QuestionGenerationPlanDto {
        val normalizedCoveredAngles =
            coveredAngles
                .normalizeAngleList()

        return copy(
            evidenceType =
                evidenceType
                    .normalizeEnumValue()
                    .take(
                        MAXIMUM_EVIDENCE_TYPE_LENGTH
                    ),
            evidenceId =
                evidenceId
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .replace(
                        WHITESPACE_REGEX,
                        " "
                    )
                    .trim()
                    .take(
                        MAXIMUM_EVIDENCE_ID_LENGTH
                    ),
            requestedAngles =
                requestedAngles
                    .normalizeAngleList()
                    .filterNot { angle ->
                        angle in
                                normalizedCoveredAngles
                    },
            coveredAngles =
                normalizedCoveredAngles,
            previousQuestionCount =
                previousQuestionCount.coerceAtLeast(
                    minimumValue = 0
                )
        )
    }

    fun toJson(): JSONObject {
        val normalized =
            normalized()

        return JSONObject().apply {
            put(
                JSON_EVIDENCE_TYPE,
                normalized.evidenceType
            )

            put(
                JSON_EVIDENCE_ID,
                normalized.evidenceId
            )

            put(
                JSON_REQUESTED_ANGLES,
                JSONArray(
                    normalized.requestedAngles
                )
            )

            put(
                JSON_COVERED_ANGLES,
                JSONArray(
                    normalized.coveredAngles
                )
            )

            put(
                JSON_PREVIOUS_QUESTION_COUNT,
                normalized
                    .previousQuestionCount
            )
        }
    }

    private fun List<String>.normalizeAngleList():
            List<String> {
        return asSequence()
            .map { angle ->
                angle.normalizeEnumValue()
            }
            .filter { angle ->
                angle in ALLOWED_ANGLES
            }
            .distinct()
            .take(
                MAXIMUM_ANGLE_COUNT
            )
            .toList()
    }

    private fun String.normalizeEnumValue():
            String {
        return replace(
            '\u0000',
            ' '
        )
            .trim()
            .uppercase(
                Locale.ROOT
            )
            .replace(
                ENUM_SEPARATOR_REGEX,
                "_"
            )
            .trim(
                '_'
            )
    }

    companion object {
        private const val JSON_EVIDENCE_TYPE =
            "evidenceType"

        private const val JSON_EVIDENCE_ID =
            "evidenceId"

        private const val JSON_REQUESTED_ANGLES =
            "requestedAngles"

        private const val JSON_COVERED_ANGLES =
            "coveredAngles"

        private const val JSON_PREVIOUS_QUESTION_COUNT =
            "previousQuestionCount"

        private const val MAXIMUM_EVIDENCE_TYPE_LENGTH =
            50

        private const val MAXIMUM_EVIDENCE_ID_LENGTH =
            120

        private const val MAXIMUM_ANGLE_COUNT =
            15

        private val ALLOWED_ANGLES =
            setOf(
                "OVERVIEW",
                "OWNERSHIP",
                "CHALLENGE",
                "DECISION",
                "TRADE_OFF",
                "FAILURE",
                "IMPACT",
                "COLLABORATION",
                "TECHNICAL_DEPTH",
                "SCALABILITY",
                "SECURITY",
                "TESTING",
                "DEPLOYMENT",
                "LEARNING",
                "REFLECTION",
                "GENERAL"
            )

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}