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

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isLearned = 1")
    fun getLearnedWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1")
    fun getLearnedWordsCount(): Flow<Int>

    @Query("UPDATE words SET isLearned = :isLearned WHERE id = :wordId")
    suspend fun updateLearnedStatus(wordId: Long, isLearned: Boolean)

    @Query("SELECT * FROM words WHERE difficulty <= :maxDifficulty ORDER BY RANDOM() LIMIT :limit")
    fun getWordsForLearning(maxDifficulty: Int, limit: Int): Flow<List<WordEntity>>

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()
}
