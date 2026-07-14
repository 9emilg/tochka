package bg.tochka.reader.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {

    @Query("SELECT * FROM saved_articles ORDER BY savedAtMillis DESC")
    fun observeAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT id FROM saved_articles")
    fun observeSavedIds(): Flow<List<Int>>

    @Query("SELECT * FROM saved_articles WHERE id = :id")
    suspend fun getById(id: Int): SavedArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedArticleEntity)

    @Delete
    suspend fun delete(entity: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteById(id: Int)
}
