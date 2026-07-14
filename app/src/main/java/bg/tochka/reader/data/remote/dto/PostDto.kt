package bg.tochka.reader.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenderedDto(
    val rendered: String = "",
)

@Serializable
data class PostDto(
    val id: Int,
    val date: String = "",
    val slug: String = "",
    val link: String = "",
    val title: RenderedDto = RenderedDto(),
    val content: RenderedDto = RenderedDto(),
    val excerpt: RenderedDto = RenderedDto(),
    val author: Int = 0,
    @SerialName("featured_media") val featuredMedia: Int = 0,
    val categories: List<Int> = emptyList(),
    @SerialName("_embedded") val embedded: EmbeddedDto? = null,
)

@Serializable
data class EmbeddedDto(
    val author: List<AuthorDto> = emptyList(),
    @SerialName("wp:featuredmedia") val featuredMedia: List<FeaturedMediaDto> = emptyList(),
    @SerialName("wp:term") val terms: List<List<TermDto>> = emptyList(),
)

@Serializable
data class AuthorDto(
    val id: Int = 0,
    val name: String = "",
)

@Serializable
data class FeaturedMediaDto(
    val id: Int = 0,
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("alt_text") val altText: String? = null,
)

@Serializable
data class TermDto(
    val id: Int = 0,
    val taxonomy: String = "",
    val name: String = "",
    val slug: String = "",
)
