package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juno.app.data.local.entity.GrammarProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarProgressDao {
    @Query("SELECT * FROM grammar_progress WHERE id = 1")
    fun getProgress(): Flow<GrammarProgressEntity?>

    @Query("SELECT * FROM grammar_progress WHERE id = 1")
    suspend fun getProgressSync(): GrammarProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: GrammarProgressEntity)
}
