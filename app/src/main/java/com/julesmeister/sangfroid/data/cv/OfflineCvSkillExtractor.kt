package com.julesmeister.sangfroid.data.cv

import com.julesmeister.sangfroid.domain.cv.CvDocument
import com.julesmeister.sangfroid.domain.cv.CvExtractionResult
import com.julesmeister.sangfroid.domain.cv.CvSkillExtractor
import com.julesmeister.sangfroid.domain.cv.CvSkillMatch
import com.julesmeister.sangfroid.domain.cv.CvTextExtraction
import com.julesmeister.sangfroid.domain.model.SkillCode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class OfflineCvSkillExtractor @Inject constructor() :
    CvSkillExtractor {

    override suspend fun extractSkills(
        document: CvDocument,
        textExtraction: CvTextExtraction
    ): Result<CvExtractionResult> {
        return withContext(
            Dispatchers.Default
        ) {
            try {
                val text =
                    textExtraction.text

                require(
                    text.isNotBlank()
                ) {
                    "CV text cannot be blank."
                }

                val normalizedText =
                    normalizeForMatching(
                        text
                    )

                val matches =
                    SkillAliases
                        .mapNotNull { definition ->
                            matchSkill(
                                definition =
                                    definition,
                                normalizedText =
                                    normalizedText,
                                originalText =
                                    text
                            )
                        }
                        .sortedWith(
                            compareByDescending<CvSkillMatch> {
                                it.confidence
                            }.thenBy {
                                it.skillCode.name
                            }
                        )

                Result.success(
                    CvExtractionResult(
                        document =
                            document,
                        extractedText =
                            text,
                        pageCount =
                            textExtraction.pageCount,
                        skills =
                            matches,
                        warnings =
                            textExtraction.warnings
                    )
                )
            } catch (
                cancellation: CancellationException
            ) {
                throw cancellation
            } catch (
                throwable: Throwable
            ) {
                Result.failure(
                    throwable
                )
            }
        }
    }

    private fun matchSkill(
        definition: SkillAliasDefinition,
        normalizedText: String,
        originalText: String
    ): CvSkillMatch? {
        val matches =
            definition.aliases
                .mapNotNull { alias ->
                    val normalizedAlias =
                        normalizeForMatching(
                            alias
                        )

                    val pattern =
                        Regex(
                            pattern =
                                "(?<![a-z0-9])" +
                                        Regex.escape(
                                            normalizedAlias
                                        ) +
                                        "(?![a-z0-9])",
                            option =
                                RegexOption.IGNORE_CASE
                        )

                    if (
                        pattern.containsMatchIn(
                            normalizedText
                        )
                    ) {
                        alias
                    } else {
                        null
                    }
                }
                .distinct()

        if (matches.isEmpty()) {
            return null
        }

        val evidence =
            matches
                .flatMap { alias ->
                    findEvidence(
                        text =
                            originalText,
                        alias =
                            alias
                    )
                }
                .distinct()
                .take(
                    MAX_EVIDENCE_ITEMS
                )

        val aliasStrength =
            matches.maxOf { alias ->
                when {
                    alias.length >= 12 -> {
                        0.94f
                    }

                    alias.length >= 6 -> {
                        0.88f
                    }

                    alias.length >= 3 -> {
                        0.80f
                    }

                    else -> {
                        0.68f
                    }
                }
            }

        val repetitionBonus =
            (
                    (matches.size - 1) *
                            0.03f
                    )
                .coerceAtMost(
                    0.06f
                )

        return CvSkillMatch(
            skillCode =
                definition.skillCode,
            confidence =
                (
                        aliasStrength +
                                repetitionBonus
                        )
                    .coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            0.99f
                    ),
            matchedAliases =
                matches,
            evidence =
                evidence
        )
    }

    private fun findEvidence(
        text: String,
        alias: String
    ): List<String> {
        return text
            .lineSequence()
            .map {
                it.trim()
            }
            .filter {
                it.contains(
                    other =
                        alias,
                    ignoreCase =
                        true
                )
            }
            .map {
                it.take(
                    MAX_EVIDENCE_LENGTH
                )
            }
            .filter {
                it.isNotBlank()
            }
            .take(
                MAX_EVIDENCE_ITEMS
            )
            .toList()
    }

    private fun normalizeForMatching(
        value: String
    ): String {
        return value
            .lowercase()
            .replace(
                "–",
                "-"
            )
            .replace(
                "—",
                "-"
            )
            .replace(
                Regex(
                    "[^a-z0-9+#./-]+"
                ),
                " "
            )
            .replace(
                Regex(
                    "\\s+"
                ),
                " "
            )
            .trim()
    }

    private data class SkillAliasDefinition(
        val skillCode: SkillCode,
        val aliases: List<String>
    )

    companion object {

        private const val MAX_EVIDENCE_ITEMS =
            3

        private const val MAX_EVIDENCE_LENGTH =
            180

        private val SkillAliases =
            listOf(
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.KOTLIN,
                    aliases =
                        listOf(
                            "Kotlin"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.ANDROID,
                    aliases =
                        listOf(
                            "Android",
                            "Android Studio",
                            "Jetpack Compose",
                            "Android SDK"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.JAVA,
                    aliases =
                        listOf(
                            "Java",
                            "Java 17",
                            "Java 21"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.SPRING_BOOT,
                    aliases =
                        listOf(
                            "Spring Boot",
                            "SpringBoot",
                            "Spring Framework"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.ANGULAR,
                    aliases =
                        listOf(
                            "Angular",
                            "AngularJS"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.REST_API,
                    aliases =
                        listOf(
                            "REST API",
                            "REST APIs",
                            "RESTful API",
                            "RESTful APIs",
                            "RESTful services"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.HTTP,
                    aliases =
                        listOf(
                            "HTTP",
                            "HTTPS"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.SQL,
                    aliases =
                        listOf(
                            "SQL",
                            "MySQL",
                            "PostgreSQL",
                            "SQLite",
                            "SQL Server"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.NOSQL,
                    aliases =
                        listOf(
                            "NoSQL",
                            "MongoDB",
                            "document database"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.DYNAMODB,
                    aliases =
                        listOf(
                            "DynamoDB",
                            "Amazon DynamoDB"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.AWS,
                    aliases =
                        listOf(
                            "AWS",
                            "Amazon Web Services",
                            "AWS Lambda",
                            "API Gateway",
                            "Amazon S3",
                            "CloudFront",
                            "ECS",
                            "Fargate",
                            "Cognito"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.GIT,
                    aliases =
                        listOf(
                            "Git",
                            "GitHub",
                            "GitLab",
                            "Bitbucket"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.CI_CD,
                    aliases =
                        listOf(
                            "CI/CD",
                            "continuous integration",
                            "continuous delivery",
                            "continuous deployment",
                            "GitHub Actions",
                            "GitLab CI",
                            "Jenkins"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.TESTING,
                    aliases =
                        listOf(
                            "unit testing",
                            "integration testing",
                            "automated testing",
                            "JUnit",
                            "Mockito",
                            "Espresso"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.SECURITY,
                    aliases =
                        listOf(
                            "security",
                            "authentication",
                            "authorization",
                            "OAuth",
                            "JWT",
                            "Cognito",
                            "IAM"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.SYSTEM_DESIGN,
                    aliases =
                        listOf(
                            "system design",
                            "software architecture",
                            "distributed systems",
                            "microservices",
                            "serverless architecture"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.COMMUNICATION,
                    aliases =
                        listOf(
                            "communication",
                            "stakeholder communication",
                            "client communication",
                            "requirements gathering"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.LEADERSHIP,
                    aliases =
                        listOf(
                            "leadership",
                            "team lead",
                            "technical lead",
                            "mentored",
                            "mentoring"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.TEAMWORK,
                    aliases =
                        listOf(
                            "teamwork",
                            "cross-functional",
                            "collaborated",
                            "collaboration"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.PROBLEM_SOLVING,
                    aliases =
                        listOf(
                            "problem solving",
                            "problem-solving",
                            "troubleshooting",
                            "root cause analysis"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.TIME_MANAGEMENT,
                    aliases =
                        listOf(
                            "time management",
                            "prioritization",
                            "prioritisation"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.ADAPTABILITY,
                    aliases =
                        listOf(
                            "adaptability",
                            "adapted to",
                            "rapidly learned"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.OWNERSHIP,
                    aliases =
                        listOf(
                            "ownership",
                            "owned delivery",
                            "end-to-end ownership",
                            "end to end ownership"
                        )
                ),
                SkillAliasDefinition(
                    skillCode =
                        SkillCode.FEEDBACK,
                    aliases =
                        listOf(
                            "feedback",
                            "code review",
                            "code reviews",
                            "peer review"
                        )
                )
            )
    }
}