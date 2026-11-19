package de.expatunity.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [
        ComplexEntity::class,
        ComplexDetailEntity::class,
        ComplexImageEntity::class,
        ApartmentEntity::class,
        FavoriteEntity::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun complexDao(): ComplexDao
    abstract fun complexDetailDao(): ComplexDetailDao
    abstract fun favoritesDao(): FavoritesDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun db(@ApplicationContext ctx: Context) =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "expatunity.db").build()

    @Provides
    fun complexDao(db: AppDatabase) = db.complexDao()
}