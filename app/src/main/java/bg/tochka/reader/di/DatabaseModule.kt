package bg.tochka.reader.di

import android.content.Context
import androidx.room.Room
import bg.tochka.reader.data.local.AppDatabase
import bg.tochka.reader.data.local.SavedArticleDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    fun provideSavedArticleDao(database: AppDatabase): SavedArticleDao = database.savedArticleDao()
}
