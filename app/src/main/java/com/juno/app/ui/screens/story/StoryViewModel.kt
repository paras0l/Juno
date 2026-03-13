package com.juno.app.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.StoryEntity
import com.juno.app.data.local.PreferencesManager
import com.juno.app.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryUiState(
    val isLoading: Boolean = true,
    val stories: List<StoryEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            combine(
                storyRepository.getAllStories(),
                preferencesManager.difficultyLevel,
                preferencesManager.storyStyle
            ) { stories, _, _ ->
                StoryUiState(
                    isLoading = false,
                    stories = stories
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                StoryUiState()
            ).collect { state ->
                _uiState.value = state
            }
        }
    }

    fun generateNewStory() {
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true

            try {
                val difficulty = preferencesManager.difficultyLevel.first()
                val style = preferencesManager.storyStyle.first()

                // Placeholder for LLM call - generate a story based on preferences
                val newStory = generatePlaceholderStory(difficulty, style)
                storyRepository.insertStory(newStory)

                _uiState.value = _uiState.value.copy(
                    stories = listOf(newStory) + _uiState.value.stories
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private suspend fun generatePlaceholderStory(level: Int, style: String): StoryEntity {
        val titles = mapOf(
            "adventure" to "The Mysterious Island Adventure",
            "mystery" to "The Case of the Missing Diamond",
            "science_fiction" to "Journey to the Stars",
            "romance" to "A Summer Love Story",
            "fairy_tale" to "The Magic Kingdom"
        )

        val contents = mapOf(
            "adventure" to """
Once upon a time, there was a young explorer named Tom who lived in a small coastal village. One day, while walking along the beach, Tom discovered an old map hidden inside a glass bottle. The map showed a path to a mysterious island that had never been visited before.

Excited by his discovery, Tom decided to set sail on his small boat. The journey was long and dangerous. He faced strong winds and big waves, but he never gave up. After three days at sea, he finally spotted the island on the horizon.

The island was beautiful and full of surprises. Strange birds flew in the sky, and colorful flowers grew everywhere. Tom landed his boat and began to explore. He found ancient ruins covered with mysterious symbols. In the center of the ruins stood a golden statue shining in the sunlight.

As Tom approached the statue, he noticed it held a precious gemstone. This was the treasure he had been searching for! Carefully, he took the gemstone and promised to bring it back to his village to share with everyone.

The adventure had taught Tom that courage and determination could help anyone achieve their dreams. He returned home a hero, with stories that would be told for generations.
            """.trimIndent(),
            "mystery" to """
Detective Sarah arrived at the grand mansion at exactly midnight. The famous actress Madame Rose had reported a diamond missing from her jewelry collection. The house was full of guests, any of whom could be the thief.

Sarah began her investigation carefully. She examined the jewelry room where the diamond had been kept. The windows were locked from inside, and the door had been locked with a special key. How did the thief get in and out?

She interviewed each guest one by one. The butler said he was cleaning the hallway. The maid was in the kitchen. The chef was preparing midnight snacks. Everyone had an alibi, but something didn't feel right.

Then Sarah noticed a small detail - a tiny piece of fabric caught on the window latch. It was part of a uniform, but not the mansion's regular uniform. She realized the thief might not be a guest at all!

After more investigation, Sarah discovered that one of the visiting staff members had been acting strangely. Under pressure, the person admitted to taking the diamond. They had hidden it in a box of snacks meant for the guests!

The diamond was returned to Madame Rose, and Sarah became famous for solving another impossible case.
            """.trimIndent(),
            "science_fiction" to """
In the year 2150, young scientist Maya was part of the first team to travel to Mars. The mission had been planned for years, and finally, the big day had come. The spacecraft Orion launched into space with great excitement.

The journey took six months. Maya watched Earth become smaller and smaller until it looked just like a blue star in the darkness. During the trip, she conducted experiments and prepared for her work on the Red Planet.

When they finally landed, Maya was the first to step onto Martian soil. The red landscape stretched endlessly before her. The sky was pinkish-orange, so different from Earth's blue sky.

The team built their base and began exploring. They discovered frozen water under the surface - a huge finding that could help future colonists. They also found strange rock formations that might be evidence of ancient microbial life.

Maya sent messages back to Earth, sharing her discoveries. Children around the world watched her adventures with wonder. Maybe one day, humans would live on Mars, and Maya would be remembered as a pioneer of space exploration.
            """.trimIndent(),
            "romance" to """
Emma moved to the small town of Willowbrook for the summer. She was hoping to escape her busy city life and find some peace. The town was charming, with old trees and friendly people.

One afternoon, Emma visited the local bookstore. There she met James, the owner's son who was home from college for the summer. They discovered they both loved reading the same books and talking about stories.

As summer days passed, Emma and James spent more time together. They walked through flower fields, watched sunsets from the hill, and shared their dreams and hopes. Emma had never felt so connected to someone.

But summer was ending, and Emma had to return to the city. On her last day, James gave her a book with a special message inside. "No matter where you go," he wrote, "you will always be part of my story."

Emma realized that some connections transcend distance. They promised to stay in touch, hoping that love would find its way back to them. Sometimes the best stories are the ones we live ourselves.
            """.trimIndent(),
            "fairy_tale" to """
Once upon a time, in a kingdom far away, there lived a young princess named Lily. She was kind and gentle, loved by everyone in the land. But Lily had a secret - she could talk to animals, a magical gift from her grandmother.

One day, a terrible dragon appeared in the kingdom. It demanded treasure and scared all the villagers. The king was worried, but no knight could defeat the fierce dragon.

Lily decided to help her people. Following a little bird, she found the dragon's cave. Instead of fighting, she talked to the dragon. She learned that the dragon was lonely and sad. Everyone was afraid of him, so he became angry.

Lily understood that sometimes, people (and dragons) just need kindness. She invited the dragon to live near the castle, where he became a friend to all. The dragon learned to play with children and help farmers with their heavy work.

The kingdom prospered, and Lily became known not just as a princess, but as someone who solved problems with her heart. The moral of the story is that understanding and kindness can change even the fiercest hearts.
            """.trimIndent()
        )

        val styleContent = contents[style] ?: contents["adventure"]!!
        val styleTitle = titles[style] ?: titles["adventure"]!!

        val difficultyTexts = mapOf(
            1 to "A Simple Beginning",
            2 to "The Journey Starts",
            3 to "An Exciting Adventure",
            4 to "The Great Challenge",
            5 to "The Ultimate Quest"
        )

        val finalTitle = "$styleTitle: ${difficultyTexts[level] ?: "A Tale"}"

        return StoryEntity(
            title = finalTitle,
            content = styleContent,
            summary = styleContent.take(100) + "...",
            level = level,
            style = style,
            wordCount = styleContent.split(" ").size,
            createdAt = System.currentTimeMillis(),
            isCompleted = false,
            isRead = false,
            readingTimeMinutes = (styleContent.split(" ").size / 200)
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
