package com.juno.app.data.remote.model

/**
 * Represents an AI tutor with unique personality and teaching style
 */
data class TutorProfile(
    val id: String,
    val name: String,
    val personality: String,  // 严谨, 热情, 幽默, etc.
    val avatar: String,       // Avatar identifier or URL
    val description: String,
    val accent: String        // English accent: american, british, australian
) {
    companion object {
        val presetTutors = listOf(
            TutorProfile(
                id = "professor_smith",
                name = "Professor Smith",
                personality = "严谨",
                avatar = "avatar_professor",
                description = "学术型教师，注重语法和精确发音，适合系统学习",
                accent = "british"
            ),
            TutorProfile(
                id = "traveler_mike",
                name = "Traveler Mike",
                personality = "热情",
                avatar = "avatar_traveler",
                description = "环球旅行者，喜欢分享实用口语和文化知识",
                accent = "american"
            ),
            TutorProfile(
                id = "comedian_emma",
                name = "Comedian Emma",
                personality = "幽默",
                avatar = "avatar_comedian",
                description = "轻松幽默的老师，用笑话和趣事帮助记忆",
                accent = "australian"
            )
        )

        fun getTutorById(id: String): TutorProfile? {
            return presetTutors.find { it.id == id }
        }
    }
}
