package bg.tochka.reader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedArticleEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedArticleDao(): SavedArticleDao

    companion object {
        const val DATABASE_NAME = "tochka_reader.db"
    }
}
