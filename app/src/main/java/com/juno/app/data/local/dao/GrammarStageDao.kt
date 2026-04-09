package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.GrammarStageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarStageDao {
    @Query("SELECT * FROM grammar_stages ORDER BY `order`")
    fun getAllStages(): Flow<List<GrammarStageEntity>>

    @Query("SELECT * FROM grammar_stages WHERE id = :id")
    suspend fun getStageById(id: Long): GrammarStageEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStage(stage: GrammarStageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStages(stages: List<GrammarStageEntity>)

    @Update
    suspend fun updateStage(stage: GrammarStageEntity)

    @Query("SELECT COUNT(*) FROM grammar_stages")
    suspend fun getStageCount(): Int
}
