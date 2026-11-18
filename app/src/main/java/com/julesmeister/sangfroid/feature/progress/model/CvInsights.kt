package com.julesmeister.sangfroid.feature.progress.model

import com.julesmeister.sangfroid.domain.cv.CvProfile
import java.util.Locale
import kotlin.math.roundToInt

data class CvInsights(
    val cvQuestionCount: Int,
    val totalQuestionCount: Int,
    val practisedCvQuestionCount: Int,
    val cvAveragePercentage: Int?,
    val generalAveragePercentage: Int?,
    val typeBreakdown: List<CvEvidenceTypeCount>,
    val evidenceItems: List<CvEvidenceProgress>,
    val coveredEvidenceCount: Int,
    val totalEvidenceCount: Int?,
    val untappedEvidence: List<CvUntappedEvidence>
) {

    val sharePercentage: Int
        get() =
            if (totalQuestionCount == 0) 0
            else (cvQuestionCount * 100f / totalQuestionCount).roundToInt()

    val coverageFraction: Float?
        get() =
            totalEvidenceCount
                ?.takeIf { it > 0 }
                ?.let { coveredEvidenceCount.toFloat() / it }
                ?.coerceIn(0f, 1f)

    val scoreDifference: Int?
        get() {
            val cv = cvAveragePercentage ?: return null
            val general = generalAveragePercentage ?: return null
            return cv - general
        }
}

data class CvEvidenceTypeCount(
    val type: String,
    val label: String,
    val count: Int
)

data class CvEvidenceProgress(
    val evidenceType: String,
    val typeLabel: String,
    val evidenceId: String,
    val title: String,
    val questionCount: Int,
    val practisedCount: Int,
    val averagePercentage: Int?,
    val nextQuestionId: String
)

data class CvUntappedEvidence(
    val evidenceType: String,
    val typeLabel: String,
    val title: String
)

object CvInsightsCalculator {

    fun calculate(
        items: List<ProgressQuestionItem>,
        cvProfile: CvProfile?
    ): CvInsights? {
        val cvItems =
            items.filter { item ->
                !item.question.evidenceType.isNullOrBlank() &&
                        !item.question.evidenceId.isNullOrBlank()
            }

        val titles =
            cvProfile?.let(::evidenceTitles).orEmpty()

        if (cvItems.isEmpty() && titles.isEmpty()) {
            return null
        }

        val generalItems =
            items - cvItems.toSet()

        val evidenceItems =
            cvItems
                .groupBy { item ->
                    EvidenceKey(
                        type = item.question.evidenceType!!.normalizeType(),
                        id = item.question.evidenceId!!.trim()
                    )
                }
                .map { (key, group) ->
                    val next =
                        group.firstOrNull { it.progress.attemptCount == 0 }
                            ?: group.minByOrNull {
                                it.progress.averageNormalizedScore ?: 0f
                            }
                            ?: group.first()

                    CvEvidenceProgress(
                        evidenceType = key.type,
                        typeLabel = typeLabel(key.type),
                        evidenceId = key.id,
                        title =
                            titles[key]
                                ?: group.first().question.generationReason
                                    ?.takeIf(String::isNotBlank)
                                ?: typeLabel(key.type),
                        questionCount = group.size,
                        practisedCount =
                            group.count { it.progress.attemptCount > 0 },
                        averagePercentage = weightedPercentage(group),
                        nextQuestionId = next.question.id
                    )
                }
                .sortedWith(
                    compareByDescending<CvEvidenceProgress> { it.questionCount }
                        .thenBy { it.title.lowercase(Locale.ROOT) }
                )

        val coveredKeys =
            evidenceItems
                .map { EvidenceKey(it.evidenceType, it.evidenceId) }
                .toSet()

        val untapped =
            titles
                .filterKeys { it !in coveredKeys }
                .map { (key, title) ->
                    CvUntappedEvidence(
                        evidenceType = key.type,
                        typeLabel = typeLabel(key.type),
                        title = title
                    )
                }

        val typeBreakdown =
            cvItems
                .groupingBy { it.question.evidenceType!!.normalizeType() }
                .eachCount()
                .map { (type, count) ->
                    CvEvidenceTypeCount(
                        type = type,
                        label = typeLabel(type),
                        count = count
                    )
                }
                .sortedByDescending { it.count }

        return CvInsights(
            cvQuestionCount = cvItems.size,
            totalQuestionCount = items.size,
            practisedCvQuestionCount =
                cvItems.count { it.progress.attemptCount > 0 },
            cvAveragePercentage = weightedPercentage(cvItems),
            generalAveragePercentage = weightedPercentage(generalItems),
            typeBreakdown = typeBreakdown,
            evidenceItems = evidenceItems,
            coveredEvidenceCount =
                if (titles.isEmpty()) {
                    coveredKeys.size
                } else {
                    coveredKeys.count { it in titles }
                },
            totalEvidenceCount = titles.size.takeIf { cvProfile != null },
            untappedEvidence = untapped
        )
    }

    private fun weightedPercentage(
        items: List<ProgressQuestionItem>
    ): Int? {
        val evaluated =
            items.filter {
                it.progress.averageNormalizedScore != null &&
                        it.progress.completedEvaluationCount > 0
            }

        val weight =
            evaluated.sumOf { it.progress.completedEvaluationCount }

        if (weight == 0) return null

        val total =
            evaluated.sumOf {
                (it.progress.averageNormalizedScore ?: 0f).toDouble() *
                        it.progress.completedEvaluationCount
            }

        return (total / weight * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    private fun evidenceTitles(
        profile: CvProfile
    ): Map<EvidenceKey, String> {
        return buildMap {
            profile.projects.forEach {
                put(EvidenceKey("PROJECT", it.id), it.name)
            }
            profile.achievements.forEach {
                put(EvidenceKey("ACHIEVEMENT", it.id), it.title)
            }
            profile.workHighlights.forEach {
                put(EvidenceKey("WORK_HIGHLIGHT", it.id), it.title)
            }
            profile.certifications.forEach {
                put(EvidenceKey("CERTIFICATION", it.id), it.name)
            }
        }
    }

    fun typeLabel(type: String): String {
        return when (type) {
            "PROJECT" -> "Project"
            "ACHIEVEMENT" -> "Achievement"
            "WORK_HIGHLIGHT" -> "Work"
            "CERTIFICATION" -> "Certification"
            "EDUCATION" -> "Education"
            else -> "CV"
        }
    }

    private fun String.normalizeType(): String {
        return trim()
            .uppercase(Locale.ROOT)
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')
    }

    private data class EvidenceKey(
        val type: String,
        val id: String
    )
}