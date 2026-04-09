package com.juno.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.juno.app.data.local.AudioPlaybackManager
import com.juno.app.data.local.GrammarDataInitializer
import com.juno.app.data.local.JunoDatabase
import com.juno.app.data.local.PreferencesManager
import com.juno.app.data.local.VoiceRecordingManager
import com.juno.app.data.local.dao.GrammarLessonDao
import com.juno.app.data.local.dao.GrammarProgressDao
import com.juno.app.data.local.dao.GrammarStageDao
import com.juno.app.data.local.dao.ReviewDao
import com.juno.app.data.local.dao.StoryDao
import com.juno.app.data.local.dao.UserProgressDao
import com.juno.app.data.local.dao.WordDao
import com.juno.app.data.repository.ReviewRepositoryImpl
import com.juno.app.data.repository.StoryRepositoryImpl
import com.juno.app.data.repository.UserRepositoryImpl
import com.juno.app.data.repository.WordRepositoryImpl
import com.juno.app.domain.repository.ReviewRepository
import com.juno.app.domain.repository.StoryRepository
import com.juno.app.domain.repository.UserRepository
import com.juno.app.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJunoDatabase(
        @ApplicationContext context: Context
    ): JunoDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE words ADD COLUMN lastStudiedDate INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE words ADD COLUMN gptContent TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS grammar_stages (
                        id INTEGER NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        level TEXT NOT NULL,
                        `order` INTEGER NOT NULL,
                        totalLessons INTEGER NOT NULL,
                        completedLessons INTEGER NOT NULL,
                        isUnlocked INTEGER NOT NULL,
                        completedAt INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS grammar_lessons (
                        id INTEGER NOT NULL PRIMARY KEY,
                        stageId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        `order` INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        examples TEXT NOT NULL,
                        exercises TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        correctRate REAL NOT NULL DEFAULT 0.0,
                        lastPracticedAt INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS grammar_progress (
                        id INTEGER NOT NULL PRIMARY KEY,
                        currentStageId INTEGER NOT NULL,
                        currentLessonId INTEGER NOT NULL,
                        totalStudyTime INTEGER NOT NULL DEFAULT 0,
                        lastStudyDate INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        return Room.databaseBuilder(
            context,
            JunoDatabase::class.java,
            JunoDatabase.DATABASE_NAME
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: JunoDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideReviewDao(database: JunoDatabase): ReviewDao {
        return database.reviewDao()
    }

    @Provides
    @Singleton
    fun provideUserProgressDao(database: JunoDatabase): UserProgressDao {
        return database.userProgressDao()
    }

    @Provides
    @Singleton
    fun provideStoryDao(database: JunoDatabase): StoryDao {
        return database.storyDao()
    }

    @Provides
    @Singleton
    fun provideGrammarStageDao(database: JunoDatabase): GrammarStageDao {
        return database.grammarStageDao()
    }

    @Provides
    @Singleton
    fun provideGrammarLessonDao(database: JunoDatabase): GrammarLessonDao {
        return database.grammarLessonDao()
    }

    @Provides
    @Singleton
    fun provideGrammarProgressDao(database: JunoDatabase): GrammarProgressDao {
        return database.grammarProgressDao()
    }

    @Provides
    @Singleton
    fun provideGrammarDataInitializer(
        @ApplicationContext context: Context,
        grammarStageDao: GrammarStageDao,
        grammarLessonDao: GrammarLessonDao
    ): GrammarDataInitializer {
        return GrammarDataInitializer(context, grammarStageDao, grammarLessonDao)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlaybackManager(
        @ApplicationContext context: Context
    ): AudioPlaybackManager {
        return AudioPlaybackManager(context)
    }

    @Provides
    @Singleton
    fun provideVoiceRecordingManager(
        @ApplicationContext context: Context
    ): VoiceRecordingManager {
        return VoiceRecordingManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository
}
