package com.julesmeister.sangfroid.data.speech

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptFormatter @Inject constructor() {

    fun format(
        transcript: String
    ): String {
        val normalized =
            transcript
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        if (normalized.isBlank()) {
            return ""
        }

        if (!looksAllCaps(normalized)) {
            return normalized
        }

        val lower =
            normalized.lowercase()

        val sentenceCased =
            lower.replaceFirstChar { character ->
                character.uppercase()
            }

        return restoreKnownTerms(
            sentenceCased
        )
    }

    private fun looksAllCaps(
        text: String
    ): Boolean {
        val letters =
            text.filter {
                it.isLetter()
            }

        if (letters.isEmpty()) {
            return false
        }

        val uppercaseCount =
            letters.count {
                it.isUpperCase()
            }

        val uppercaseRatio =
            uppercaseCount.toFloat() /
                    letters.length.toFloat()

        return uppercaseRatio >=
                ALL_CAPS_THRESHOLD
    }

    private fun restoreKnownTerms(
        text: String
    ): String {
        var formatted =
            text

        KNOWN_TERMS.forEach {
                (normalized, canonical) ->

            formatted =
                formatted.replace(
                    Regex(
                        "\\b${Regex.escape(normalized)}\\b",
                        RegexOption.IGNORE_CASE
                    ),
                    canonical
                )
        }

        return formatted
    }

    companion object {

        private const val ALL_CAPS_THRESHOLD =
            0.90f

        private val KNOWN_TERMS =
            linkedMapOf(
                "aws" to "AWS",
                "api" to "API",
                "apis" to "APIs",
                "rest" to "REST",
                "restful" to "RESTful",
                "sql" to "SQL",
                "nosql" to "NoSQL",
                "jwt" to "JWT",
                "oauth" to "OAuth",
                "oauth2" to "OAuth2",
                "http" to "HTTP",
                "https" to "HTTPS",
                "json" to "JSON",
                "xml" to "XML",
                "sdk" to "SDK",
                "ui" to "UI",
                "ux" to "UX",
                "ci" to "CI",
                "cd" to "CD",
                "ci/cd" to "CI/CD",
                "s3" to "S3",
                "ec2" to "EC2",
                "ecs" to "ECS",
                "ecr" to "ECR",
                "sqs" to "SQS",
                "sns" to "SNS",
                "iam" to "IAM",
                "lambda" to "Lambda",
                "dynamodb" to "DynamoDB",
                "cloudfront" to "CloudFront",
                "cognito" to "Cognito",
                "spring boot" to "Spring Boot",
                "springboot" to "Spring Boot",
                "java" to "Java",
                "kotlin" to "Kotlin",
                "android" to "Android",
                "docker" to "Docker",
                "kubernetes" to "Kubernetes",
                "angular" to "Angular",
                "typescript" to "TypeScript",
                "javascript" to "JavaScript",
                "github" to "GitHub",
                "git" to "Git"
            )
    }
}