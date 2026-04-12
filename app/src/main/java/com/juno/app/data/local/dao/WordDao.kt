package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE LOWER(word) = LOWER(:wordText) LIMIT 1")
    suspend fun getWordByText(wordText: String): WordEntity?

    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordByIdFlow(id: Long): Flow<WordEntity?>

    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isLearned = 0 ORDER BY RANDOM() LIMIT :limit")
    fun getUnlearnedWords(limit: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    fun getWordsByDifficulty(difficulty: Int, limit: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category")
    fun getWordsByCategory(category: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE (word LIKE '%' || :query || '%' OR definitions LIKE '%' || :query || '%')")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isLearned = 1 AND (word LIKE '%' || :query || '%' OR definitions LIKE '%' || :query || '%')")
    fun searchLearnedWords(query: String): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records r ON w.id = r.wordId
        WHERE r.repetitions >= 3 AND (w.word LIKE '%' || :query || '%' OR w.definitions LIKE '%' || :query || '%')
    """)
    fun searchMasteredWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isLearned = 1")
    fun getLearnedWords(): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN review_records r ON w.id = r.wordId
        WHERE r.repetitions >= 3
    """)
    fun getMasteredWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1")
    fun getLearnedWordsCount(): Flow<Int>

    @Query("UPDATE words SET isLearned = :isLearned WHERE id = :wordId")
    suspend fun updateLearnedStatus(wordId: Long, isLearned: Boolean)

    @Query("SELECT * FROM words WHERE difficulty <= :maxDifficulty ORDER BY RANDOM() LIMIT :limit")
    fun getWordsForLearning(maxDifficulty: Int, limit: Int): Flow<List<WordEntity>>

    @Query("UPDATE words SET lastStudiedDate = :date WHERE id = :wordId")
    suspend fun updateLastStudiedDate(wordId: Long, date: Long)

    @Query("SELECT * FROM words WHERE lastStudiedDate IS NOT NULL ORDER BY lastStudiedDate DESC LIMIT :limit")
    fun getRecentlyStudiedWords(limit: Int): Flow<List<WordEntity>>

    @Query("SELECT word FROM words")
    suspend fun getAllWordsList(): List<String>

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT * FROM words WHERE id = :id AND gptContent IS NOT NULL LIMIT 1")
    suspend fun getWordWithGptContent(id: Long): WordEntity?
}
