package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.model.CvEvidenceAngleUsageRow
import com.julesmeister.sangfroid.data.local.model.CvEvidenceUsageRow
import com.julesmeister.sangfroid.domain.model.QuestionAngle
import com.julesmeister.sangfroid.domain.recommendation.CvEvidence
import com.julesmeister.sangfroid.domain.recommendation.CvEvidenceType
import com.julesmeister.sangfroid.domain.recommendation.QuestionGenerationPlan
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreshQuestionPlanner @Inject constructor() {

    fun createPlans(
        evidence: List<CvEvidence>,
        evidenceUsage: List<CvEvidenceUsageRow>,
        angleUsage: List<CvEvidenceAngleUsageRow>,
        requestedQuestionCount: Int
    ): List<QuestionGenerationPlan> {
        require(requestedQuestionCount > 0) {
            "requestedQuestionCount must be positive."
        }

        val usageByEvidence =
            evidenceUsage.associateBy { usage ->
                EvidenceKey(
                    type =
                        usage.evidenceType.normalizeType(),
                    id =
                        usage.evidenceId.trim()
                )
            }

        val angleUsageByEvidence =
            angleUsage.groupBy { usage ->
                EvidenceKey(
                    type =
                        usage.evidenceType.normalizeType(),
                    id =
                        usage.evidenceId.trim()
                )
            }

        val plans =
            evidence
                .distinctBy { item ->
                    EvidenceKey(
                        type = item.type.name,
                        id = item.id
                    )
                }
                .map { item ->
                    val key =
                        EvidenceKey(
                            type = item.type.name,
                            id = item.id
                        )

                    val coveredAngles =
                        angleUsageByEvidence[
                            key
                        ]
                            .orEmpty()
                            .filter { usage ->
                                usage.usageCount > 0
                            }
                            .map { usage ->
                                usage.questionAngle
                            }
                            .toSet()

                    val supportedAngles =
                        supportedAngles(
                            item
                        )

                    val unusedAngles =
                        supportedAngles.filterNot {
                                angle ->
                            angle in coveredAngles
                        }

                    QuestionGenerationPlan(
                        evidence = item,
                        requestedAngles =
                            (
                                    unusedAngles +
                                            supportedAngles.filter {
                                                    angle ->
                                                angle in coveredAngles
                                            }
                                    )
                                .distinct()
                                .take(
                                    ANGLES_PER_EVIDENCE
                                ),
                        coveredAngles =
                            coveredAngles
                                .sortedBy(
                                    QuestionAngle::ordinal
                                ),
                        previousQuestionCount =
                            usageByEvidence[
                                key
                            ]
                                ?.usageCount
                                ?: 0
                    )
                }
                .sortedWith(
                    compareBy<
                            QuestionGenerationPlan
                            > { plan ->
                        plan.previousQuestionCount
                    }
                        .thenByDescending { plan ->
                            plan.requestedAngles.count {
                                    angle ->
                                angle !in
                                        plan.coveredAngles
                            }
                        }
                        .thenBy { plan ->
                            plan.evidence.title.lowercase(
                                Locale.ROOT
                            )
                        }
                )

        if (plans.isEmpty()) {
            return emptyList()
        }

        val desiredEvidenceCount =
            requestedQuestionCount
                .coerceAtMost(
                    plans.size
                )
                .coerceAtLeast(1)

        return plans.take(
            desiredEvidenceCount
        )
    }

    private fun supportedAngles(
        evidence: CvEvidence
    ): List<QuestionAngle> {
        val base =
            mutableListOf(
                QuestionAngle.OVERVIEW,
                QuestionAngle.OWNERSHIP,
                QuestionAngle.CHALLENGE,
                QuestionAngle.DECISION,
                QuestionAngle.TRADE_OFF,
                QuestionAngle.IMPACT,
                QuestionAngle.LEARNING,
                QuestionAngle.REFLECTION
            )

        if (
            evidence.actions.isNotEmpty() ||
            evidence.methods.isNotEmpty()
        ) {
            base +=
                QuestionAngle.TECHNICAL_DEPTH
        }

        if (evidence.tools.isNotEmpty()) {
            base +=
                QuestionAngle.TESTING

            base +=
                QuestionAngle.DEPLOYMENT

            base +=
                QuestionAngle.SECURITY

            base +=
                QuestionAngle.SCALABILITY
        }

        if (
            evidence.outcomes.isNotEmpty() ||
            evidence.metrics.isNotEmpty()
        ) {
            base +=
                QuestionAngle.IMPACT
        }

        if (
            evidence.type ==
            CvEvidenceType.WORK_HIGHLIGHT
        ) {
            base +=
                QuestionAngle.COLLABORATION
        }

        if (
            evidence.type ==
            CvEvidenceType.PROJECT
        ) {
            base +=
                QuestionAngle.FAILURE
        }

        return base.distinct()
    }

    private fun String.normalizeType():
            String {
        return trim()
            .uppercase(Locale.ROOT)
            .replace(
                NON_ENUM_CHARACTER_REGEX,
                "_"
            )
            .trim('_')
    }

    private data class EvidenceKey(
        val type: String,
        val id: String
    )

    companion object {
        private const val ANGLES_PER_EVIDENCE =
            5

        private val NON_ENUM_CHARACTER_REGEX =
            Regex("[^A-Z0-9]+")
    }
}