package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.model.CvEvidenceUsageRow
import com.julesmeister.sangfroid.domain.recommendation.CvEvidence
import com.julesmeister.sangfroid.domain.recommendation.CvEvidenceType
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvEvidenceSelector @Inject constructor() {

    fun select(
        evidence: List<CvEvidence>,
        usage: List<CvEvidenceUsageRow>,
        requestedQuestionCount: Int
    ): List<CvEvidence> {
        if (
            evidence.isEmpty() ||
            requestedQuestionCount <= 0
        ) {
            return emptyList()
        }

        val normalizedEvidence =
            evidence
                .asSequence()
                .map(
                    CvEvidence::normalized
                )
                .filter { item ->
                    item.id.isNotBlank() &&
                            item.title.isNotBlank()
                }
                .distinctBy { item ->
                    EvidenceKey(
                        type =
                            item.type,
                        id =
                            item.id
                    )
                }
                .toList()

        if (normalizedEvidence.isEmpty()) {
            return emptyList()
        }

        val usageByEvidence =
            usage
                .asSequence()
                .mapNotNull { row ->
                    val type =
                        CvEvidenceType.fromName(
                            row.evidenceType
                        )
                            ?: return@mapNotNull null

                    val evidenceId =
                        row.evidenceId
                            .trim()
                            .takeIf(
                                String::isNotBlank
                            )
                            ?: return@mapNotNull null

                    EvidenceKey(
                        type =
                            type,
                        id =
                            evidenceId
                    ) to
                            Usage(
                                count =
                                    row.usageCount
                                        .coerceAtLeast(
                                            0
                                        ),
                                lastUsedAt =
                                    row.lastUsedAt
                                        ?.takeIf { timestamp ->
                                            timestamp > 0L
                                        }
                            )
                }
                .toMap()

        val rankedEvidence =
            normalizedEvidence
                .sortedWith(
                    compareBy<CvEvidence> { item ->
                        usageFor(
                            item =
                                item,
                            usageByEvidence =
                                usageByEvidence
                        ).count
                    }
                        .thenBy { item ->
                            usageFor(
                                item =
                                    item,
                                usageByEvidence =
                                    usageByEvidence
                            ).lastUsedAt
                                ?: NEVER_USED_TIMESTAMP
                        }
                        .thenByDescending { item ->
                            evidenceQualityScore(
                                item
                            )
                        }
                        .thenBy { item ->
                            typePriority(
                                item.type
                            )
                        }
                        .thenBy { item ->
                            item.title
                                .lowercase(
                                    Locale.ROOT
                                )
                        }
                        .thenBy { item ->
                            item.id
                                .lowercase(
                                    Locale.ROOT
                                )
                        }
                )

        val maximumEvidenceCount =
            calculateMaximumEvidenceCount(
                requestedQuestionCount =
                    requestedQuestionCount,
                availableEvidenceCount =
                    rankedEvidence.size
            )

        val selected =
            LinkedHashMap<
                    EvidenceKey,
                    CvEvidence
                    >()

        /*
         * First pass: provide type diversity. Within each type, the first
         * entry is already the least-used and strongest eligible item.
         */
        CvEvidenceType.entries
            .sortedBy(
                ::typePriority
            )
            .forEach { type ->
                if (
                    selected.size >=
                    maximumEvidenceCount
                ) {
                    return@forEach
                }

                rankedEvidence
                    .firstOrNull { item ->
                        item.type == type
                    }
                    ?.let { item ->
                        selected[
                            item.key()
                        ] =
                            item
                    }
            }

        /*
         * Second pass: fill remaining capacity globally using usage,
         * recency, evidence strength, type, title, and ID ordering.
         */
        rankedEvidence.forEach { item ->
            if (
                selected.size >=
                maximumEvidenceCount
            ) {
                return@forEach
            }

            selected.putIfAbsent(
                item.key(),
                item
            )
        }

        return selected
            .values
            .toList()
    }

    private fun usageFor(
        item: CvEvidence,
        usageByEvidence:
        Map<EvidenceKey, Usage>
    ): Usage {
        return usageByEvidence[
            item.key()
        ] ?: Usage()
    }

    private fun CvEvidence.key():
            EvidenceKey {
        return EvidenceKey(
            type =
                type,
            id =
                id
        )
    }

    private fun evidenceQualityScore(
        evidence: CvEvidence
    ): Int {
        var score =
            0

        if (evidence.metrics.isNotEmpty()) {
            score +=
                METRIC_SCORE
        }

        if (evidence.outcomes.isNotEmpty()) {
            score +=
                OUTCOME_SCORE
        }

        if (evidence.actions.isNotEmpty()) {
            score +=
                ACTION_SCORE
        }

        if (evidence.methods.isNotEmpty()) {
            score +=
                METHOD_SCORE
        }

        if (evidence.tools.isNotEmpty()) {
            score +=
                TOOL_SCORE
        }

        if (evidence.relatedSkills.isNotEmpty()) {
            score +=
                RELATED_SKILL_SCORE
        }

        if (!evidence.context.isNullOrBlank()) {
            score +=
                CONTEXT_SCORE
        }

        if (!evidence.candidateRole.isNullOrBlank()) {
            score +=
                ROLE_SCORE
        }

        if (!evidence.evidence.isNullOrBlank()) {
            score +=
                SOURCE_EVIDENCE_SCORE
        }

        score +=
            when (
                evidence.type
            ) {
                CvEvidenceType.ACHIEVEMENT ->
                    ACHIEVEMENT_SCORE

                CvEvidenceType.PROJECT ->
                    PROJECT_SCORE

                CvEvidenceType.WORK_HIGHLIGHT ->
                    WORK_HIGHLIGHT_SCORE

                CvEvidenceType.CERTIFICATION ->
                    CERTIFICATION_SCORE

                CvEvidenceType.EDUCATION ->
                    EDUCATION_SCORE
            }

        return score
    }

    private fun typePriority(
        type: CvEvidenceType
    ): Int {
        return when (
            type
        ) {
            CvEvidenceType.ACHIEVEMENT ->
                0

            CvEvidenceType.PROJECT ->
                1

            CvEvidenceType.WORK_HIGHLIGHT ->
                2

            CvEvidenceType.CERTIFICATION ->
                3

            CvEvidenceType.EDUCATION ->
                4
        }
    }

    private fun calculateMaximumEvidenceCount(
        requestedQuestionCount: Int,
        availableEvidenceCount: Int
    ): Int {
        val requestedWindow =
            when (
                requestedQuestionCount
                    .coerceIn(
                        1,
                        MAXIMUM_REQUESTED_QUESTION_COUNT
                    )
            ) {
                1 ->
                    4

                2 ->
                    5

                3 ->
                    6

                4 ->
                    7

                else ->
                    MAXIMUM_SELECTED_EVIDENCE_COUNT
            }

        return requestedWindow
            .coerceAtMost(
                availableEvidenceCount
            )
    }

    private data class EvidenceKey(
        val type: CvEvidenceType,
        val id: String
    )

    private data class Usage(
        val count: Int = 0,
        val lastUsedAt: Long? = null
    )

    companion object {
        private const val MAXIMUM_REQUESTED_QUESTION_COUNT =
            10

        private const val MAXIMUM_SELECTED_EVIDENCE_COUNT =
            8

        private const val NEVER_USED_TIMESTAMP =
            Long.MIN_VALUE

        private const val METRIC_SCORE =
            12

        private const val OUTCOME_SCORE =
            10

        private const val ACTION_SCORE =
            8

        private const val METHOD_SCORE =
            5

        private const val TOOL_SCORE =
            5

        private const val RELATED_SKILL_SCORE =
            4

        private const val CONTEXT_SCORE =
            3

        private const val ROLE_SCORE =
            2

        private const val SOURCE_EVIDENCE_SCORE =
            2

        private const val ACHIEVEMENT_SCORE =
            5

        private const val PROJECT_SCORE =
            4

        private const val WORK_HIGHLIGHT_SCORE =
            3

        private const val CERTIFICATION_SCORE =
            2

        private const val EDUCATION_SCORE =
            1
    }
}