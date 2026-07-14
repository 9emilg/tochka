package bg.tochka.reader.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String = "",
    val slug: String = "",
    val count: Int = 0,
)
