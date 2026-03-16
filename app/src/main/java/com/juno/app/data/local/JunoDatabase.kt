package com.juno.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juno.app.data.local.dao.ReviewDao
import com.juno.app.data.local.dao.StoryDao
import com.juno.app.data.local.dao.UserProgressDao
import com.juno.app.data.local.dao.WordDao
import com.juno.app.data.local.entity.ReviewRecordEntity
import com.juno.app.data.local.entity.StoryEntity
import com.juno.app.data.local.entity.UserProgressEntity
import com.juno.app.data.local.entity.WordEntity

@Database(
    entities = [
        WordEntity::class,
        ReviewRecordEntity::class,
        UserProgressEntity::class,
        StoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class JunoDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun storyDao(): StoryDao

    companion object {
        const val DATABASE_NAME = "juno_database"
    }
}
