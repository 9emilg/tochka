package bg.tochka.reader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val id: Int,
    val slug: String,
    val link: String,
    val title: String,
    val excerpt: String,
    val contentHtml: String,
    val author: String,
    val dateIso: String,
    val dateDisplay: String,
    val categoriesCsv: String,
    val primaryCategory: String?,
    val imageUrl: String?,
    val readMinutes: Int,
    val savedAtMillis: Long,
)
