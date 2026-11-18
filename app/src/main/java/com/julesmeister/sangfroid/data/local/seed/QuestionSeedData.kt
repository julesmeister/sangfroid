package com.julesmeister.sangfroid.data.local.seed

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionSkillCrossRef
import com.julesmeister.sangfroid.domain.model.Difficulty
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode

object QuestionSeedData {

    val questions = listOf(
        QuestionEntity(
            id = "hr_tell_me_about_yourself",
            question = "Tell me about yourself.",
            category = QuestionCategory.HR,
            difficulty = Difficulty.FOUNDATION,
            whyAsked = "The interviewer wants a concise professional summary connecting your background, relevant experience, and motivation for the role.",
            answerFramework = "Present, past, future",
            estimatedMinutes = 2,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "hr_why_this_role",
            question = "Why are you interested in this role?",
            category = QuestionCategory.HR,
            difficulty = Difficulty.FOUNDATION,
            whyAsked = "The interviewer wants evidence that you understand the position and have a deliberate reason for applying.",
            answerFramework = "Role, evidence, contribution",
            estimatedMinutes = 2,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "hr_why_hire_you",
            question = "Why should we hire you?",
            category = QuestionCategory.HR,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer wants you to connect your strongest evidence directly to the employer's requirements.",
            answerFramework = "Requirement, evidence, value",
            estimatedMinutes = 3,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "behavioral_technical_problem",
            question = "Tell me about a time you had to solve a difficult technical problem.",
            category = QuestionCategory.BEHAVIORAL,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer wants to understand how you approach ambiguity, make technical decisions, and communicate your reasoning.",
            answerFramework = "STAR",
            estimatedMinutes = 4,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "behavioral_conflict",
            question = "Tell me about a disagreement you had with a teammate.",
            category = QuestionCategory.BEHAVIORAL,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer wants to understand how you handle disagreement while maintaining professional collaboration.",
            answerFramework = "STAR",
            estimatedMinutes = 4,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "behavioral_mistake",
            question = "Tell me about a mistake you made and how you handled it.",
            category = QuestionCategory.BEHAVIORAL,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer is evaluating accountability, recovery, communication, and learning.",
            answerFramework = "STAR plus reflection",
            estimatedMinutes = 4,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "technical_rest_api",
            question = "What makes a REST API well designed?",
            category = QuestionCategory.TECHNICAL,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer wants to assess your understanding of resource modeling, HTTP semantics, consistency, security, and maintainability.",
            answerFramework = "Principle, example, trade-off",
            estimatedMinutes = 5,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "technical_database_choice",
            question = "How would you decide between a relational database and a NoSQL database?",
            category = QuestionCategory.TECHNICAL,
            difficulty = Difficulty.INTERMEDIATE,
            whyAsked = "The interviewer wants to see whether you evaluate data relationships, consistency, query patterns, scale, and operational trade-offs.",
            answerFramework = "Requirements, comparison, decision",
            estimatedMinutes = 5,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "technical_android_offline_first",
            question = "How would you design an offline-first Android application?",
            category = QuestionCategory.TECHNICAL,
            difficulty = Difficulty.ADVANCED,
            whyAsked = "The interviewer wants to assess local persistence, synchronization, conflict handling, state management, and reliability.",
            answerFramework = "Architecture, data flow, failure handling",
            estimatedMinutes = 7,
            source = "BUILT_IN"
        ),
        QuestionEntity(
            id = "system_design_interview_practice",
            question = "Design an interview-practice application that supports offline use and cloud synchronization.",
            category = QuestionCategory.SYSTEM_DESIGN,
            difficulty = Difficulty.ADVANCED,
            whyAsked = "The interviewer wants to evaluate requirements clarification, architecture, data ownership, synchronization, scalability, privacy, and trade-offs.",
            answerFramework = "Requirements, architecture, data, scale, trade-offs",
            estimatedMinutes = 15,
            source = "BUILT_IN"
        )
    )

    val questionSkills = listOf(
        QuestionSkillCrossRef(
            questionId = "hr_tell_me_about_yourself",
            skillCode = SkillCode.SELF_INTRODUCTION
        ),
        QuestionSkillCrossRef(
            questionId = "hr_tell_me_about_yourself",
            skillCode = SkillCode.COMMUNICATION
        ),
        QuestionSkillCrossRef(
            questionId = "hr_why_this_role",
            skillCode = SkillCode.MOTIVATION
        ),
        QuestionSkillCrossRef(
            questionId = "hr_why_hire_you",
            skillCode = SkillCode.COMMUNICATION
        ),
        QuestionSkillCrossRef(
            questionId = "behavioral_technical_problem",
            skillCode = SkillCode.PROBLEM_SOLVING
        ),
        QuestionSkillCrossRef(
            questionId = "behavioral_technical_problem",
            skillCode = SkillCode.COMMUNICATION
        ),
        QuestionSkillCrossRef(
            questionId = "behavioral_conflict",
            skillCode = SkillCode.CONFLICT_RESOLUTION
        ),
        QuestionSkillCrossRef(
            questionId = "behavioral_conflict",
            skillCode = SkillCode.TEAMWORK
        ),
        QuestionSkillCrossRef(
            questionId = "behavioral_mistake",
            skillCode = SkillCode.OWNERSHIP
        ),
        QuestionSkillCrossRef(
            questionId = "technical_rest_api",
            skillCode = SkillCode.REST_API
        ),
        QuestionSkillCrossRef(
            questionId = "technical_rest_api",
            skillCode = SkillCode.HTTP
        ),
        QuestionSkillCrossRef(
            questionId = "technical_database_choice",
            skillCode = SkillCode.SQL
        ),
        QuestionSkillCrossRef(
            questionId = "technical_database_choice",
            skillCode = SkillCode.NOSQL
        ),
        QuestionSkillCrossRef(
            questionId = "technical_android_offline_first",
            skillCode = SkillCode.ANDROID
        ),
        QuestionSkillCrossRef(
            questionId = "technical_android_offline_first",
            skillCode = SkillCode.KOTLIN
        ),
        QuestionSkillCrossRef(
            questionId = "system_design_interview_practice",
            skillCode = SkillCode.SYSTEM_DESIGN
        ),
        QuestionSkillCrossRef(
            questionId = "system_design_interview_practice",
            skillCode = SkillCode.AWS
        )
    )
}