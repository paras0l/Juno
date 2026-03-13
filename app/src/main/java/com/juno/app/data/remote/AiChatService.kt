package com.juno.app.data.remote

import com.juno.app.data.remote.model.ChatMessage
import com.juno.app.data.remote.model.TutorProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatService @Inject constructor() {

    private val tutors = TutorProfile.presetTutors

    private val greetingTemplates = mapOf(
        "professor_smith" to listOf(
            "Good day! Let's begin our English lesson.",
            "Welcome back. Today we'll focus on precision and accuracy.",
            "Greetings. Shall we continue your studies?"
        ),
        "traveler_mike" to listOf(
            "Hey there! Ready for some real English?",
            "What's up! Let's chat like locals do.",
            "Yo! Let's dive into some cool expressions!"
        ),
        "comedian_emma" to listOf(
            "Hey hey! Prepare to laugh while you learn!",
            "Welcome, welcome! Hope you're ready for some fun!",
            "Hiya! Let's make learning English a blast!"
        )
    )

    private val questionTemplates = mapOf(
        "professor_smith" to listOf(
            "Could you please elaborate on '{word}'?",
            "What is the correct usage of '{word}' in a sentence?",
            "Define '{word}' and provide an example."
        ),
        "traveler_mike" to listOf(
            "Hey, how would you use '{word}' in real life?",
            "What's the deal with '{word}'? Give me a real example!",
            "Got any cool ways to use '{word}'?"
        ),
        "comedian_emma" to listOf(
            "So '{word}' walks into a bar... wait, how would you use it?",
            "Can you make me laugh using '{word}'?",
            "What's the funniest way to use '{word}'?"
        )
    )

    private val responseTemplates = mapOf(
        "professor_smith" to mapOf(
            "good" to "Excellent work! Your understanding is precise.",
            "partial" to "Good effort, but let's refine your answer.",
            "needs_improvement" to "Let's review this concept more carefully."
        ),
        "traveler_mike" to mapOf(
            "good" to "Awesome! You're getting the hang of it!",
            "partial" to "Not bad! But there's a more natural way to say it.",
            "needs_improvement" to "No worries, let me show you how natives say it."
        ),
        "comedian_emma" to mapOf(
            "good" to "Hahaha, brilliant! You're a natural comedian!",
            "partial" to "Haha, close! But here's a funnier version...",
            "needs_improvement" to "Aw, don't worry! Even I stumble sometimes. Try this!"
        )
    )

    fun getAllTutors(): List<TutorProfile> = tutors

    fun getTutorById(tutorId: String): TutorProfile? {
        return tutors.find { it.id == tutorId }
    }

    fun generateGreeting(tutorId: String): String {
        val templates = greetingTemplates[tutorId] ?: greetingTemplates["professor_smith"]!!
        return templates.random()
    }

    fun generateProactiveQuestion(
        tutorId: String,
        learnedWords: List<String>,
        @Suppress("UNUSED_PARAMETER") userLevel: Int
    ): String {
        getTutorById(tutorId) ?: getAllTutors().first()
        val templates = questionTemplates[tutorId] ?: questionTemplates["professor_smith"]!!
        
        val word = if (learnedWords.isNotEmpty()) {
            learnedWords.random()
        } else {
            "hello"
        }

        return templates.random().replace("{word}", word)
    }

    fun processUserResponse(message: String, context: List<ChatMessage>): String {
        val tutorId = inferTutorFromContext(context) ?: "professor_smith"
        
        val responseQuality = evaluateResponseQuality(message)
        
        val response = responseTemplates[tutorId]?.get(responseQuality) 
            ?: responseTemplates["professor_smith"]?.get(responseQuality)!!
        
        return response
    }

    private fun inferTutorFromContext(context: List<ChatMessage>): String? {
        return context.firstOrNull { !it.isFromUser }?.let { msg ->
            when {
                msg.content.contains("Good day") || msg.content.contains("Excellent") || msg.content.contains("precise") -> "professor_smith"
                msg.content.contains("Hey") || msg.content.contains("awesome") || msg.content.contains("native") -> "traveler_mike"
                msg.content.contains("hahaha") || msg.content.contains("funny") || msg.content.contains("laugh") -> "comedian_emma"
                else -> null
            }
        }
    }

    private fun evaluateResponseQuality(response: String): String {
        val lowerResponse = response.lowercase()
        
        if (lowerResponse.length > 10 && lowerResponse.contains(" ") && lowerResponse.contains(".")) {
            return "good"
        }
        
        if (lowerResponse.length > 5) {
            return "partial"
        }
        
        return "needs_improvement"
    }

    fun createGreetingMessage(tutorId: String): ChatMessage {
        return ChatMessage(
            content = generateGreeting(tutorId),
            isFromUser = false
        )
    }
}
